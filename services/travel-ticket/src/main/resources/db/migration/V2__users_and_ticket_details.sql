alter table travel_tickets
  add column if not exists employee_name varchar(80) not null default '',
  add column if not exists department varchar(120) not null default '',
  add column if not exists trip_purpose varchar(240) not null default '',
  add column if not exists attachment_status varchar(32) not null default 'UPLOADED';

create index if not exists idx_tickets_tenant_employee_name
  on travel_tickets (tenant_id, employee_name);
