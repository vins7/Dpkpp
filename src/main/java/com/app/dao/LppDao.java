package com.app.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.app.model.Lpp;
import com.app.pojo.PojoPersonLkh;

@Repository
public class LppDao extends BaseDao implements BaseMasterDao{

	@Override
	public <T> void save(T entity) throws Exception {
		em.persist(entity);
	}

	@Override
	public <T> void edit(T entity) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <T> void delete(T entity) throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	@SuppressWarnings("unchecked")
	public Lpp getById(String id) throws Exception{
		List<Lpp> results = em.createQuery("FROM Lpp where id = :id")
				.setParameter("id", id)
				.getResultList();
		
		return !results.isEmpty() ? results.get(0) : null;
	}

	
}