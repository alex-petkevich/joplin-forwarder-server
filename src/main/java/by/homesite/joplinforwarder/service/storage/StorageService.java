package by.homesite.joplinforwarder.service.storage;

import by.homesite.joplinforwarder.model.Mail;
import by.homesite.joplinforwarder.model.User;

public interface StorageService {
    void storeRecord(User user, Mail mail);
}
