package com.example.FinalSpringPract.dao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.FinalSpringPract.entity.User;



public interface MainRepository extends JpaRepository<User, Integer>{
	
	@Query("select u from User u where u.email=:email")
	public User getUserByName(@Param("email") String email) ;
}
