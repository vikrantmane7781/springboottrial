package com.example.FinalSpringPract.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.example.FinalSpringPract.entity.Contact;
import com.example.FinalSpringPract.entity.User;

public interface ContactRepository extends JpaRepository<Contact,Integer> {
	@Query("from Contact as c where c.user.id =:userId")
	public Page<Contact> findContactsByUser(@Param("userId")int userId,Pageable pager);
	
	public List<Contact>findByNameContainingAndUser(String name,User user);
}
