package by.homesite.joplinforwarder.service.storage;

import by.homesite.joplinforwarder.model.Mail;
import by.homesite.joplinforwarder.model.User;
import org.springframework.stereotype.Component;

@Component
public class LocalStorageService implements StorageService {

    @Override
    public void storeRecord(User user, Mail mail) {

    }
}
