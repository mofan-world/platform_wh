package com.codex.travel.ticket.config;

public final class TravelTicketAuthorities {

    public static final String CAN_READ = "hasAnyAuthority("
            + "'ADMIN',"
            + "'TRAVEL_ADMIN',"
            + "'TRAVEL_USER',"
            + "'TRAVEL_APPROVER',"
            + "'TRAVEL_AUDITOR',"
            + "'travel:ticket:read',"
            + "'travel:risk:read',"
            + "'travel:ops:read'"
            + ")";

    public static final String CAN_CREATE = "hasAnyAuthority("
            + "'ADMIN',"
            + "'TRAVEL_ADMIN',"
            + "'TRAVEL_USER',"
            + "'travel:ticket:create'"
            + ")";

    public static final String CAN_UPDATE = "hasAnyAuthority("
            + "'ADMIN',"
            + "'TRAVEL_ADMIN',"
            + "'TRAVEL_USER',"
            + "'TRAVEL_APPROVER',"
            + "'travel:ticket:update'"
            + ")";

    public static final String CAN_DELETE = "hasAnyAuthority("
            + "'ADMIN',"
            + "'TRAVEL_ADMIN',"
            + "'travel:ticket:delete'"
            + ")";

    public static final String CAN_APPROVE = "hasAnyAuthority("
            + "'ADMIN',"
            + "'TRAVEL_ADMIN',"
            + "'TRAVEL_APPROVER',"
            + "'travel:ticket:approve'"
            + ")";

    public static final String CAN_REINDEX = "hasAnyAuthority("
            + "'ADMIN',"
            + "'TRAVEL_ADMIN',"
            + "'travel:search:reindex'"
            + ")";

    public static final String CAN_READ_OPS = "hasAnyAuthority("
            + "'ADMIN',"
            + "'TRAVEL_ADMIN',"
            + "'travel:ops:read'"
            + ")";

    private TravelTicketAuthorities() {
    }
}
