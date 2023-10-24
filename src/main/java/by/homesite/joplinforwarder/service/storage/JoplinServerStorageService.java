package by.homesite.joplinforwarder.service.storage;

import by.homesite.joplinforwarder.model.Mail;
import by.homesite.joplinforwarder.model.User;
import org.springframework.stereotype.Component;

@Component
public class JoplinServerStorageService implements StorageService{

    @Override
    public String storeRecord(User user, Mail mail) {

        return null;
    }
}
