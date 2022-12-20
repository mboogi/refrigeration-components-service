CREATE TABLE polynomial_coefficients(
    id          SERIAL PRIMARY KEY,
    description VARCHAR(255) NOT NULL,
    c1 DOUBLE   PRECISION NOT NULL,
    c2 DOUBLE   PRECISION NOT NULL,
    c3 DOUBLE   PRECISION NOT NULL,
    c4 DOUBLE   PRECISION NOT NULL,
    c5 DOUBLE   PRECISION NOT NULL,
    c6 DOUBLE   PRECISION NOT NULL,
    c7 DOUBLE   PRECISION NOT NULL,
    c8 DOUBLE   PRECISION NOT NULL,
    c9 DOUBLE   PRECISION NOT NULL,
    c10 DOUBLE  PRECISION NOT NULL
);
CREATE TABLE capacity_polynomial_mapping(
    id          SERIAL PRIMARY KEY,
    capacity DOUBLE   PRECISION NOT NULL,
    polynomial_id BIGINT NOT NULL
);
CREATE TABLE compressor_polynomial_mappings(
    id          SERIAL PRIMARY KEY,
    compressor_type VARCHAR(255) NOT NULL,
    polynomial_id BIGINT NOT NULL
);


CREATE TABLE operation_type_polynomial_mapping(
    id          SERIAL PRIMARY KEY,
    trans_critical Boolean NOT NULL,
    polynomial_id BIGINT NOT NULL
);


CREATE TABLE refrigerant_polynomial_mapping(
    id          SERIAL PRIMARY KEY,
    refrigerant_type VARCHAR(255) NOT NULL,
    polynomial_id BIGINT NOT NULL
);

CREATE TABLE frequency_polynomial_mapping(
    id          SERIAL PRIMARY KEY,
    frequency DOUBLE   PRECISION NOT NULL,
    polynomial_id BIGINT NOT NULL
);
--CREATE TYPE polynomial_types AS ENUM ('MASS_FLOW', 'CURRENT', 'REFRIGERATION_POWER');
CREATE TABLE polynomial_type(
    id              SERIAL PRIMARY KEY,
    polynomial_type            VARCHAR(255)    NOT NULL,
    polynomial_id   BIGINT NOT NULL
);

CREATE VIEW polynomial_mappings_view as
    select row_number() OVER () AS id,
    x.polynomial_id,
    y.refrigerant_type,
    x.compressor_type,
    z.capacity,
    f.frequency,
    pt.polynomial_type,
    ot.trans_critical
        from compressor_polynomial_mappings as x
        left join  refrigerant_polynomial_mapping as y on x.polynomial_id=y.polynomial_id
        left  join capacity_polynomial_mapping as z on y.polynomial_id=z.polynomial_id
        left  join frequency_polynomial_mapping as f on z.polynomial_id=f.polynomial_id
        left join polynomial_type as pt on f.polynomial_id=pt.polynomial_id
        left join operation_type_polynomial_mapping as ot on pt.polynomial_id=ot.polynomial_id;


CREATE TABLE hydraulic_pipe(
    id              SERIAL PRIMARY KEY,
    name            VARCHAR(255),
    standard        VARCHAR(255)    NOT NULL,
    outer_diameter  DOUBLE   PRECISION NOT NULL,
    inner_diameter  DOUBLE   PRECISION NOT NULL,
    material        VARCHAR(255)    NOT NULL,
    max_pressure    DOUBLE   PRECISION NOT NULL
);

CREATE TYPE  VALVE_TYPE as ENUM ('EXPANSION_VALVE', 'LIQUID_INJECTION', 'HOT_GAS_BYPASS', 'SUCTION_THROTTLING', 'HEAD_PRESSURE');
CREATE TYPE  FLOW_PATTERN as ENUM ('UNI_FLOW', 'BI_FLOW');


CREATE TABLE  valve_entity(
    id                          SERIAL PRIMARY KEY,
    refrigerant                 VARCHAR(255) NOT NULL,
    valve_name                  VARCHAR(255) NOT NULL,
    pressure_drop               DOUBLE PRECISION NOT NULL,
    kvs                         DOUBLE PRECISION NOT NULL,
    condensing_temperature      DOUBLE PRECISION NOT NULL,
    evaporating_temperature     DOUBLE PRECISION NOT NULL,
    max_pressure                DOUBLE PRECISION NOT NULL,
    type                        VARCHAR(255) NOT NULL,
    flow_pattern                VARCHAR(255) NOT NULL,
    capacity_range              DOUBLE PRECISION NOT NULL,
    inlet_connection            VARCHAR(255) NOT NULL,
    outlet_connection           VARCHAR(255) NOT NULL,
    max_liquid_temperature      DOUBLE PRECISION NOT NULL,
    max_gas_temperature         DOUBLE PRECISION NOT NULL,
    refrigeration_capacity      DOUBLE PRECISION NOT NULL
)


