package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    @Autowired
    private TelegramBot telegramBot;

    @Autowired
    private NotificationTaskRepository repository;

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {

        Pattern pattern = Pattern.compile("(^\\d{2}\\.\\d{2}\\.\\d{4}\\s\\d{2}:\\d{2})(\\s)(.+$)");

        updates.forEach(update -> {
            logger.info("Processing update: {}", update);
            Long chatId = update.message().chat().id();
            // Process your updates here
            if (update.message().text().equals("/start")){

                SendMessage sendMessage = new SendMessage(chatId,"Hello my friend! Set your task!");
                telegramBot.execute(sendMessage);

            }

            String message = update.message().text();
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


        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

}
