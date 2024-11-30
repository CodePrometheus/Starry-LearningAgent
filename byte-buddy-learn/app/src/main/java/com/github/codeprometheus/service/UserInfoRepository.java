package com.github.codeprometheus.service;

import com.github.codeprometheus.entity.UserInfoDO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserInfoRepository extends JpaRepository<UserInfoDO, Integer> {
}