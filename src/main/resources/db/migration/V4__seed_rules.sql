insert into regional_base_fee_rules (city, vehicle_type, fee) values
  (0, 0, 4.0),   -- TALLINN / CAR
  (0, 1, 3.5),   -- TALLINN / SCOOTER
  (0, 2, 3.0),   -- TALLINN / BIKE
  (1, 0, 3.5),   -- TARTU   / CAR
  (1, 1, 3.0),   -- TARTU   / SCOOTER
  (1, 2, 2.5),   -- TARTU   / BIKE
  (2, 0, 3.0),   -- PÄRNU   / CAR
  (2, 1, 2.5),   -- PÄRNU   / SCOOTER
  (2, 2, 2.0);   -- PÄRNU   / BIKE



insert into weather_condition_fee_rules
(type, min_value, max_value, string_value, fee, is_allowed, vehicle_type) values
  -- AIR_TEMPERATURE
  (0, null, -10.0, null, 1.0,  true,  1),  -- < -10, SCOOTER
  (0, null, -10.0, null, 1.0,  true,  2),  -- < -10, BIKE
  (0, -10.0, 0.0,  null, 0.5,  true,  1),  -- -10..0, SCOOTER
  (0, -10.0, 0.0,  null, 0.5,  true,  2),  -- -10..0, BIKE
  -- WIND_SPEED
  (1, 10.0, 20.0,  null, 0.5,  true,  2),  -- 10..20, BIKE
  (1, 20.0, null,  null, -1.0, false, 2),  -- > 20 (forbidden), BIKE
  -- WEATHER_PHENOMENON
  (2, null, null, 'snow',    1.0, true,  1),  -- snow,    SCOOTER
  (2, null, null, 'snow',    1.0, true,  2),  -- snow,    BIKE
  (2, null, null, 'sleet',   1.0, true,  1),  -- sleet,   SCOOTER
  (2, null, null, 'sleet',   1.0, true,  2),  -- sleet,   BIKE
  (2, null, null, 'rain',    0.5, true,  1),  -- rain,    SCOOTER
  (2, null, null, 'rain',    0.5, true,  2),  -- rain,    BIKE
  (2, null, null, 'glaze',   0.5, false, 1),  -- glaze (forbidden),   SCOOTER
  (2, null, null, 'glaze',   0.5, false, 2),  -- glaze (forbidden),   BIKE
  (2, null, null, 'hail',    0.5, false, 1),  -- hail (forbidden),    SCOOTER
  (2, null, null, 'hail',    0.5, false, 2),  -- hail (forbidden),    BIKE
  (2, null, null, 'thunder', 0.5, false, 1),  -- thunder (forbidden), SCOOTER
  (2, null, null, 'thunder', 0.5, false, 2);  -- thunder (forbidden), BIKE