package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.internal.matchers.Null;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.junit4.SpringRunner;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.atLeastOnce;


@SpringBootTest
@AutoConfigureTestDatabase
class NotificationTaskServiceTest {

    private final String START_MESSAGE = "Hello my friend! Set your task!";

    @Autowired
    private NotificationTaskRepository repository;
    @MockBean
    private Message message;
    @MockBean
    private SendResponse sendResponse;
    @MockBean
    private TelegramBot telegramBot;

    @InjectMocks
    @Autowired
    private NotificationTaskService out;


    @AfterEach
    public void clean(){
        repository.deleteAll();
    }

    // Null value test
    @Test
    void replyStartTestNull(){
        assertNull(out.replyStart(null));
    }

    // Negative value test
    @Test
    void replyStartTestNegative(){
        assertNull(out.replyStart(-1L));
    }

    // Valid value test
    @Test
    void replyStartTestValid(){
        when(telegramBot.execute(any())).thenReturn(sendResponse);
        when(sendResponse.message()).thenReturn(message);
        when(message.text()).thenReturn("Hello my friend! Set your task!");

        assertNotNull(out.replyStart(1L));
        assertEquals(START_MESSAGE, out.replyStart(1L).message().text());
        verify(telegramBot, atLeastOnce()).execute(any());
    }

    // Message that doesn't match our pattern, null message, null chatId and chatId < 0
    public static Stream<Arguments> setTestParams() {
        return Stream.of(
                Arguments.of(1L,"Some message"),
                Arguments.of(1L,null),
                Arguments.of(null,"Some message"),
                Arguments.of(null,null),
                Arguments.of(-1L,"")
        );
    }
    @ParameterizedTest
    @MethodSource("setTestParams")
    void saveTaskTestWrongMessage(Long chatId, String message){
        assertNull(out.saveTask(chatId,message));
    }

    // Message that matches our pattern
    @Test
    void saveTaskValidMessage(){

        NotificationTask task = new NotificationTask(1L,"Сделать домашнюю работу",
                LocalDateTime.parse("01.01.2022 20:00", DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));

        when(telegramBot.execute(any())).thenReturn(sendResponse);

        when(telegramBot.execute(any())).thenReturn(sendResponse);
        when(sendResponse.message()).thenReturn(message);
        when(message.text()).thenReturn("New task added successfully!");

        SendResponse responseOut = out.saveTask(1L,"01.01.2022 20:00 Сделать домашнюю работу"); // Saved Task to H2 database

        assertEquals("New task added successfully!", responseOut.message().text()); // Checked Success message

        assertNotNull(responseOut); // Check not null

        List<NotificationTask> list = repository.findAll();
        assertFalse(list.isEmpty());
        assertEquals(1,list.size());
        assertEquals("Сделать домашнюю работу",list.get(0).getText()); // Check that we have the correct task stored
    }

    // Send Tasks to telegram
    @Test
    void findTasksTest() throws InterruptedException{
        NotificationTask task = new NotificationTask(1L,"Сделать домашнюю работу",
                LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));

        repository.save(task);

        out.findTasks();
        List<NotificationTask> list1 = repository.findTasks(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
        assertFalse(list1.isEmpty()); // Check that we get list with tasks with LocalDateTime.now()

        verify(telegramBot,atLeastOnce()).execute(any()); // Check that we execute telegramBot.execute() method

    }

}