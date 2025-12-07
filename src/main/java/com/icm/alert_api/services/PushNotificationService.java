package com.icm.alert_api.services;

import com.icm.alert_api.models.AlertModel;

public interface PushNotificationService {
    void sendNewAlert(AlertModel alert);
}
