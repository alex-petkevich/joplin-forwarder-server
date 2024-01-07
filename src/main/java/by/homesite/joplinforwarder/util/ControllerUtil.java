package by.homesite.joplinforwarder.util;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Sort;

public class ControllerUtil
{
	private ControllerUtil() {
		// empty
	}

	public static Sort getSortOrder(String sort)
	{
		List<Sort.Order> result = new ArrayList<>();
		String[] inpSortList = sort.split(",");
		for (String order: inpSortList)
		{
			String[] inpSort = order.split("-");
			Sort.Order sortOrder = new Sort.Order(
					inpSort.length > 1 && "desc".equals(inpSort[1]) ? Sort.Direction.DESC : Sort.Direction.ASC, inpSort[0]);
			result.add(sortOrder);
		}
		return Sort.by(result);
	}

}
