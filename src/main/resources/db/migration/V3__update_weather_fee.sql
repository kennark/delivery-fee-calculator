alter table weather_condition_fee_rules
    alter column fee float(53) null;

alter table weather_condition_fee_rules
    add column is_allowed boolean not null default false;

alter table weather_condition_fee_rules
    add column vehicle_type tinyint check ((vehicle_type between 0 and 2));