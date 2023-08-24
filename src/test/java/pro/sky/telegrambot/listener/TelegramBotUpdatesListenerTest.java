package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.sky.telegrambot.repository.NotificationTaskRepository;
import pro.sky.telegrambot.service.NotificationTaskService;


import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TelegramBotUpdatesListenerTest {

    @Mock
    private Update update;
    @Mock
    private Message message;
    @Mock
    private Chat chat;
    @Mock
    private NotificationTaskService service;

    @InjectMocks
    private TelegramBotUpdatesListener out;

    @BeforeEach
    public void init(){
        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
    }

    // All valid values for "/start" message
    @Test
    void processTestStartMessage(){
        when(chat.id()).thenReturn(1L);
        when(message.text()).thenReturn("/start");

        assertEquals(out.process(List.of(update)), -1); // at the end the method "process" should return constant value CONFIRMED_UPDATES_ALL = -1
        verify(service, atLeastOnce()).replyStart(1L); // checking if "replyStart" method was called at least once
    }

    // Using -1L value as chatId to call sendSomethingWrong method
    @Test
    void processTestStartWrongChatId(){
        when(chat.id()).thenReturn(-1L);
        when(message.text()).thenReturn("/start");
        when(service.replyStart(-1L)).thenReturn(null);

        assertEquals(out.process(List.of(update)), -1); // at the end method "process" should return constant value CONFIRMED_UPDATES_ALL = -1
        verify(service, atLeastOnce()).sendSomethingWrong(-1L); // checking if "sendSomethingWrong" method was called at least once
    }

    // All valid values for Task message e.g. "01.01.2022 20:00 Сделать домашнюю работу"
    @Test
    void processTestTaskMessage() {
        when(chat.id()).thenReturn(1L);
        when(message.text()).thenReturn("01.01.2022 20:00 Сделать домашнюю работу");

        assertEquals(out.process(List.of(update)), -1); // at the end the method "process" should return constant value CONFIRMED_UPDATES_ALL = -1
        verify(service, atLeastOnce()).saveTask(any(),any()); // checking if "saveTask" method was called at least once
    }

    // Using -1L value as chatId to call sendSomethingWrong method
    @Test
    void processTestTaskMessageWrongChatID() {
        when(chat.id()).thenReturn(-1L);
        when(message.text()).thenReturn("01.01.2022 20:00 Сделать домашнюю работу");
        when(service.saveTask(-1L,"01.01.2022 20:00 Сделать домашнюю работу")).thenReturn(null);

        assertEquals(out.process(List.of(update)), -1); // at the end the method "process" should return constant value CONFIRMED_UPDATES_ALL = -1
        verify(service, atLeastOnce()).sendSomethingWrong(-1L); // checking if "sendSomethingWrong" method was called at least once
    }

    // Using text that doesn't match our  to call sendSomethingWrong method
    @Test
    void processTestTaskMessageWrongText() {
        when(chat.id()).thenReturn(1L);
        when(message.text()).thenReturn("Сделать работу");
        when(service.saveTask(1L,"Сделать работу")).thenReturn(null);

        assertEquals(out.process(List.of(update)), -1); // at the end the method "process" should return constant value CONFIRMED_UPDATES_ALL = -1
        verify(service, atLeastOnce()).sendSomethingWrong(1L); // checking if "sendSomethingWrong" method was called at least once
    }

}