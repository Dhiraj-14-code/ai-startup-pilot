package com.ai_startuppilot.backend.enums;

public enum RiskStatus {
    OPEN       ,//  → Risk identified but work hasn't started
    IN_PROGRESS  ,//→ Team is currently working on it
    MITIGATED   ,// → Risk impact has been reduced/controlled
    CLOSED     //  → Risk is no longer an active issue

}
