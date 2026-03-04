package com.bsp.service;

import com.bsp.dto.request.WalkInRequest;
import com.bsp.entity.WalkInSession;

import java.util.List;

public interface WalkInService {

    List<WalkInSession> getTodaySessions();

    WalkInSession createSession(WalkInRequest request);

    WalkInSession finishSession(Long id);
}