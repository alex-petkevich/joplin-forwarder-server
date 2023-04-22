package by.homesite.joplinforwarder.util;

import java.util.List;

public interface BasicMapper<D, E>
{
	E toEntity(D dto);

	D toDto(E entity);

	List<E> toEntity(List<D> dtoList);

	List<D> toDto(List<E> entityList);
}
