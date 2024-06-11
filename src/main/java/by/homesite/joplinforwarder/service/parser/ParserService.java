package by.homesite.joplinforwarder.service.parser;

import org.springframework.stereotype.Service;

@Service
public interface ParserService
{
	void parseMail();

	void deleteOldItems(int purgeMailsPeriod);
}
