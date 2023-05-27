ALTER TABLE hydraulic_pipe ADD column producer VARCHAR(255) NOT NULL DEFAULT 'GENERIC';
ALTER TABLE hydraulic_pipe ADD CONSTRAINT name_material_unique UNIQUE (name,material,standard,producer);
