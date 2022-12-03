package com.example.application.repository;


import com.example.application.model.MessageModel;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends CrudRepository<MessageModel, Long> {
}
