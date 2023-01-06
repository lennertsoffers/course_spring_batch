package com.christianoette._C_listeners._02_job_execution_listener_component;

import lombok.Getter;
import lombok.Setter;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.stereotype.Component;

@Component
@JobScope
@Getter
@Setter
public class JobResultHolder {
    private String result;
}
