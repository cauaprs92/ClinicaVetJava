-- ==========================================================
-- ClinicaVet · dados iniciais
-- Só insere quando a tabela está vazia, então pode rodar a cada início
-- sem duplicar nada. Também é usado por "Restaurar dados de exemplo".
-- ==========================================================

-- Os 10 horários do dia: 08:00 a 12:00 de manhã e 14:00 a 18:00 à tarde.
INSERT INTO horario (numero, hora, turno)
SELECT numero, hora::time, turno
FROM (VALUES
    (1, '08:00', 'manha'),
    (2, '09:00', 'manha'),
    (3, '10:00', 'manha'),
    (4, '11:00', 'manha'),
    (5, '12:00', 'manha'),
    (6, '14:00', 'tarde'),
    (7, '15:00', 'tarde'),
    (8, '16:00', 'tarde'),
    (9, '17:00', 'tarde'),
    (10, '18:00', 'tarde')
) AS inicial (numero, hora, turno)
WHERE NOT EXISTS (SELECT 1 FROM horario);

INSERT INTO produto (nome, preco, quantidade)
SELECT nome, preco, quantidade
FROM (VALUES
    (1, 'Vacina polivalente', 90.00, 15),
    (2, 'Vacina antirrábica', 60.00, 20),
    (3, 'Vermífugo',          35.00, 18),
    (4, 'Antipulgas',         55.00, 12),
    (5, 'Anti-inflamatório',  28.00, 16)
) AS inicial (ordem, nome, preco, quantidade)
WHERE NOT EXISTS (SELECT 1 FROM produto)
ORDER BY ordem;
