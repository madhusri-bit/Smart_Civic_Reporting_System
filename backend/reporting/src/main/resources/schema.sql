ALTER TABLE issue DROP CONSTRAINT IF EXISTS issue_category_check;
ALTER TABLE issue ADD CONSTRAINT issue_category_check CHECK (
    category IN (
        'ROAD_INFRA',
        'WATER_SUPPLY',
        'SOLID_WASTE',
        'ELECTRICITY',
        'PARKS',
        'PUBLIC_SAFETY',
        'URBAN_PLANNING',
        'ANIMAL_CONTROL',
        'PUBLIC_HEALTH',
        'OTHER',
        'ROAD',
        'WATER',
        'GARBAGE',
        'WASTE',
        'GENERAL'
    )
);

ALTER TABLE issue DROP CONSTRAINT IF EXISTS issue_predicted_category_check;
ALTER TABLE issue ADD CONSTRAINT issue_predicted_category_check CHECK (
    predicted_category IN (
        'ROAD_INFRA',
        'WATER_SUPPLY',
        'SOLID_WASTE',
        'ELECTRICITY',
        'PARKS',
        'PUBLIC_SAFETY',
        'URBAN_PLANNING',
        'ANIMAL_CONTROL',
        'PUBLIC_HEALTH',
        'OTHER',
        'ROAD',
        'WATER',
        'GARBAGE',
        'WASTE',
        'GENERAL'
    )
);

ALTER TABLE issue DROP CONSTRAINT IF EXISTS issue_status_check;
ALTER TABLE issue ADD CONSTRAINT issue_status_check CHECK (
    status IN (
        'PENDING_CLASSIFICATION',
        'REPORTED',
        'MANUAL_REVIEW',
        'ASSIGNED',
        'IN_PROGRESS',
        'RESOLVED'
    )
);
