package com.group.xlibris.loan;

import org.springframework.modulith.NamedInterface;

@NamedInterface(value = "api")
public enum LoanStatus {
    ACTIVE,
    RETURNED,
    OVERDUE
}
