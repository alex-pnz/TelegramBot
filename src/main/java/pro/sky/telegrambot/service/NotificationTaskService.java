package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import liquibase.pro.packaged.L;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class NotificationTaskService {

    private SendMessage sendMessage = null;
    private Pattern pattern = Pattern.compile("(^\\d{2}\\.\\d{2}\\.\\d{4}\\s\\d{2}:\\d{2})(\\s)(.+$)");


    private final NotificationTaskRepository repository;
    private final TelegramBot telegramBot;

    public NotificationTaskService(NotificationTaskRepository repository, TelegramBot telegramBot) {
        this.repository = repository;
        this.telegramBot = telegramBot;
    }

    public boolean replyStart(Long chatId) {
        if (chatId != null && chatId >= 0) {
            sendMessage = new SendMessage(chatId,"Hello my friend! Set your task!");
            telegramBot.execute(sendMessage);
            return true;
        }
        return false;
    }

    public boolean saveTask(Long chatId, String message) {
        if (chatId != null && chatId >= 0 && message != null) {
            Matcher matcher = pattern.matcher(message);

            String date_time = null, text = null;

            if (matcher.find()) {
                date_time = matcher.group(1);
                text = matcher.group(3);
            }

            if (date_time != null && text != null) {
                NotificationTask task = new NotificationTask(chatId,
                        text,
                        LocalDateTime.parse(date_time,
                                DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));

                repository.save(task);
            }

            sendMessage = new SendMessage(chatId,"New task added successfully!");
            telegramBot.execute(sendMessage);
            return true;
        }
        return false;
    }

    public void sendSomethingWrong(Long chatId){
        sendMessage = new SendMessage(chatId,"Something went wrong!");
        telegramBot.execute(sendMessage);
    }

    @Scheduled(cron = "0 0/1 * * * *")
    public void findTasks() {
        List<NotificationTask> list = repository.findTasks(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
        if (!list.isEmpty()) {
            System.out.println(list.get(0).getText());
        }
    }


}
