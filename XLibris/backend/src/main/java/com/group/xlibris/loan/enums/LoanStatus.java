package com.group.xlibris.loan.enums;

import org.springframework.modulith.NamedInterface;

@NamedInterface(value = "api")
public enum LoanStatus {
    ACTIVE,
    RETURNED,
    OVERDUE
}
