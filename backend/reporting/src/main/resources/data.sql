INSERT INTO department (name, description)
SELECT 'Road & Infrastructure Department', 'Public Works Department (PWD)'
WHERE NOT EXISTS (
    SELECT 1 FROM department WHERE name = 'Road & Infrastructure Department'
);

INSERT INTO department (name, description)
SELECT 'Water Supply & Sanitation Department', 'Water Supply & Drainage'
WHERE NOT EXISTS (
    SELECT 1 FROM department WHERE name = 'Water Supply & Sanitation Department'
);

INSERT INTO department (name, description)
SELECT 'Solid Waste Management Department', 'Solid Waste Management'
WHERE NOT EXISTS (
    SELECT 1 FROM department WHERE name = 'Solid Waste Management Department'
);

INSERT INTO department (name, description)
SELECT 'Electricity Board', 'Electricity & Power'
WHERE NOT EXISTS (
    SELECT 1 FROM department WHERE name = 'Electricity Board'
);

INSERT INTO department (name, description)
SELECT 'Parks & Recreation Department', 'Parks & Public Spaces'
WHERE NOT EXISTS (
    SELECT 1 FROM department WHERE name = 'Parks & Recreation Department'
);

INSERT INTO department (name, description)
SELECT 'Police / Public Safety Department', 'Law & Order / Public Safety'
WHERE NOT EXISTS (
    SELECT 1 FROM department WHERE name = 'Police / Public Safety Department'
);

INSERT INTO department (name, description)
SELECT 'Urban Planning & Development Authority', 'Building & Urban Planning'
WHERE NOT EXISTS (
    SELECT 1 FROM department WHERE name = 'Urban Planning & Development Authority'
);

INSERT INTO department (name, description)
SELECT 'Animal Control Department', 'Animal Control & Veterinary'
WHERE NOT EXISTS (
    SELECT 1 FROM department WHERE name = 'Animal Control Department'
);

INSERT INTO department (name, description)
SELECT 'Public Health Department', 'Public Health'
WHERE NOT EXISTS (
    SELECT 1 FROM department WHERE name = 'Public Health Department'
);
