package com.codex.travel.ticket.config;

public final class TravelTicketAuthorities {

    public static final String CAN_READ = "hasAnyAuthority("
            + "'ROLE_ADMIN',"
            + "'ROLE_TRAVEL_ADMIN',"
            + "'ROLE_TRAVEL_USER',"
            + "'ROLE_TRAVEL_APPROVER',"
            + "'ROLE_TRAVEL_AUDITOR',"
            + "'travel:ticket:read',"
            + "'travel:risk:read',"
            + "'travel:ops:read'"
            + ")";

    public static final String CAN_CREATE = "hasAnyAuthority('ROLE_ADMIN', 'ROLE_TRAVEL_ADMIN', 'ROLE_TRAVEL_USER', 'travel:ticket:create')";

    public static final String CAN_UPDATE = "hasAnyAuthority("
            + "'ROLE_ADMIN',"
            + "'ROLE_TRAVEL_ADMIN',"
            + "'ROLE_TRAVEL_USER',"
            + "'ROLE_TRAVEL_APPROVER',"
            + "'travel:ticket:update'"
            + ")";

    public static final String CAN_DELETE = "hasAnyAuthority("
            + "'ROLE_ADMIN',"
            + "'ROLE_TRAVEL_ADMIN',"
            + "'travel:ticket:delete'"
            + ")";

    public static final String CAN_APPROVE = "hasAnyAuthority("
            + "'ROLE_ADMIN',"
            + "'ROLE_TRAVEL_ADMIN',"
            + "'ROLE_TRAVEL_APPROVER',"
            + "'travel:ticket:approve'"
            + ")";

    public static final String CAN_REINDEX = "hasAnyAuthority('ROLE_ADMIN','ROLE_TRAVEL_ADMIN','travel:search:reindex')";

    public static final String CAN_READ_OPS = "hasAnyAuthority('ROLE_ADMIN', 'ROLE_TRAVEL_ADMIN', 'travel:ops:read')";

    private TravelTicketAuthorities() {
    }
}
