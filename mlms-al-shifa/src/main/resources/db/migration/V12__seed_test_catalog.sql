-- Seed test catalog with common lab tests
-- Department IDs are based on V1 insertion order:
-- 1=Hematology, 2=Biochemistry, 3=Microbiology, 4=Immunology, 5=Urinalysis

INSERT INTO test_catalog (test_code, test_name, department_id, sample_type, turnaround_hours, price, normal_range, unit) VALUES

-- Hematology (dept 1)
('HEM-001', 'Complete Blood Count (CBC)',         1, 'Blood', 4,  2500.00, 'See report',      'cells/μL'),
('HEM-002', 'Hemoglobin',                         1, 'Blood', 2,  1500.00, 'M:13-17, F:12-16','g/dL'),
('HEM-003', 'Blood Smear',                        1, 'Blood', 6,  2000.00, 'Normal morphology','—'),
('HEM-004', 'ESR (Erythrocyte Sedimentation)',    1, 'Blood', 4,  1500.00, 'M:<15, F:<20',    'mm/hr'),
('HEM-005', 'Platelet Count',                     1, 'Blood', 2,  1500.00, '150,000–450,000', '/μL'),
('HEM-006', 'Prothrombin Time (PT)',              1, 'Blood', 4,  2500.00, '11–13.5',         'seconds'),

-- Biochemistry (dept 2)
('BIO-001', 'Blood Glucose (Fasting)',            2, 'Blood', 2,  1500.00, '70–100',          'mg/dL'),
('BIO-002', 'HbA1c',                             2, 'Blood', 4,  3500.00, '<5.7%',           '%'),
('BIO-003', 'Lipid Panel',                       2, 'Blood', 6,  4500.00, 'See report',      'mg/dL'),
('BIO-004', 'Liver Function Tests (LFT)',        2, 'Blood', 6,  5000.00, 'See report',      'U/L'),
('BIO-005', 'Renal Function Tests (RFT)',        2, 'Blood', 6,  4500.00, 'See report',      'mmol/L'),
('BIO-006', 'Serum Uric Acid',                  2, 'Blood', 4,  2000.00, 'M:3.4-7.0, F:2.4-6.0','mg/dL'),
('BIO-007', 'Troponin I',                        2, 'Blood', 2,  5000.00, '<0.04',           'ng/mL'),
('BIO-008', 'D-Dimer',                           2, 'Blood', 4,  6000.00, '<0.5',            'mg/L'),
('BIO-009', 'Thyroid Function (TSH)',            2, 'Blood', 8,  4000.00, '0.4–4.0',         'mIU/L'),
('BIO-010', 'Electrolytes Panel',               2, 'Blood', 4,  3000.00, 'See report',      'mmol/L'),

-- Microbiology (dept 3)
('MIC-001', 'Malaria Rapid Test (RDT)',          3, 'Blood', 1,  2500.00, 'Negative',        '—'),
('MIC-002', 'Blood Culture',                    3, 'Blood', 48, 5000.00, 'No growth',       '—'),
('MIC-003', 'Urine Culture & Sensitivity',      3, 'Urine', 48, 5000.00, 'No growth',       '—'),
('MIC-004', 'Typhoid (Widal Test)',             3, 'Blood', 4,  2500.00, 'Negative',        '—'),
('MIC-005', 'Stool Culture',                    3, 'Stool', 48, 3500.00, 'No pathogen',     '—'),
('MIC-006', 'GeneXpert (TB)',                   3, 'Sputum',4,  8000.00, 'Not detected',    '—'),

-- Immunology (dept 4)
('IMM-001', 'HIV 1 & 2 (Rapid)',               4, 'Blood', 1,  3000.00, 'Non-reactive',    '—'),
('IMM-002', 'Hepatitis B Surface Antigen',      4, 'Blood', 2,  3000.00, 'Non-reactive',    '—'),
('IMM-003', 'Hepatitis C Antibody',            4, 'Blood', 2,  3000.00, 'Non-reactive',    '—'),
('IMM-004', 'COVID-19 Antigen Test',           4, 'NP Swab',1, 3500.00, 'Negative',        '—'),
('IMM-005', 'CRP (C-Reactive Protein)',        4, 'Blood', 4,  2500.00, '<5',              'mg/L'),
('IMM-006', 'ANA (Antinuclear Antibody)',      4, 'Blood', 8,  5000.00, 'Negative',        '—'),

-- Urinalysis (dept 5)
('URI-001', 'Urinalysis (Routine)',            5, 'Urine', 2,  1500.00, 'See report',      '—'),
('URI-002', 'Urine Pregnancy Test (HCG)',      5, 'Urine', 1,  2000.00, 'Negative',        '—'),
('URI-003', 'Urine Microalbumin',              5, 'Urine', 4,  3000.00, '<30',             'mg/g'),
('URI-004', '24-Hour Urine Protein',          5, 'Urine', 6,  3500.00, '<150',            'mg/day');
