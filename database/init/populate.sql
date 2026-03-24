-- 1. INFRASTRUCTURE & FOUNDATIONS
INSERT OR IGNORE INTO restaurante (id, morada) VALUES ('12345', 'Braga');

INSERT OR IGNORE INTO tipo_utilizador (id, descricao) VALUES 
(0, 'COO'), (1, 'Gerente'), (2, 'Funcionario');

INSERT OR IGNORE INTO etapa (id,nome) VALUES
(0,'COZINHAQUENTE'),(1,'BANCADAFRIA'),(2,'MONTAGEM'),(3,'FINALIZACAO');

-- 2. WORKSTATIONS & ETAPAS
-- Mapping Java Enums: COZINHAQUENTE=0, BANCADAFRIA=1, MONTAGEM=2, FINALIZACAO=3
INSERT OR IGNORE INTO posto (id, nome, restaurante) VALUES 
('P_FORNO', 'Cozinheiro', '12345'),
('P_FOGAO', 'Empregado', '12345'),
('P_GOD', 'Chefe cozinha', '12345');

INSERT OR IGNORE INTO etapas_posto (posto, restaurante, etapa) VALUES 
('P_FORNO', '12345', '0'), ('P_FORNO', '12345', '1'), ('P_FORNO', '12345', '2'),
('P_FOGAO', '12345', '3'),
('P_GOD', '12345', '0'), ('P_GOD', '12345', '1'), ('P_GOD', '12345', '2'), ('P_GOD', '12345', '3');

-- 3. INGREDIENTS & INVENTORY
INSERT OR IGNORE INTO ingrediente (id, nome, preco) VALUES 
('bife_vazia', 'Bife da Vazia', 5.50), ('lombo_salmao', 'Lombo de Salmão', 6.00),
('frango_peito', 'Peito de Frango', 3.00), ('carne_picada', 'Carne Picada Bovino', 2.50),
('seitan', 'Seitan Fresco', 2.80), ('pao_burger', 'Pão de Hambúrguer Brioche', 0.80),
('arroz_basmati', 'Arroz Basmati', 0.40), ('mix_vegetais', 'Mix de Vegetais', 1.20),
('flor_sal', 'Flor de Sal', 0.10), ('pimenta_preta', 'Pimenta Preta', 0.10);

INSERT OR IGNORE INTO stock (id, restaurante) VALUES ('STK_BRAGA', '12345');

INSERT OR IGNORE INTO stock_ingrediente (stock, ingrediente, quantidade) VALUES 
('STK_BRAGA', 'bife_vazia', 50), ('STK_BRAGA', 'carne_picada', 100), ('STK_BRAGA', 'seitan', 40),
('STK_BRAGA', 'flor_sal', 500), ('STK_BRAGA', 'pimenta_preta', 200);

-- 4. ITEMS & PRODUCTS
INSERT OR IGNORE INTO item (id, nome, preco, desconto, tipo) VALUES 
('prato_001', 'Bife da Vazia Grelhado', 18.00, 0.0, 'Produto'),
('prato_004', 'Hambúrguer Customizável', 10.50, 0.0, 'Produto');

INSERT OR IGNORE INTO produto (id) VALUES ('prato_001'), ('prato_004');
INSERT OR IGNORE INTO itens_restaurante (restaurante, item) VALUES ('12345', 'prato_001'), ('12345', 'prato_004');

-- 5. TAREFAS (Removing 'estado' column per updated schema)
INSERT OR IGNORE INTO tarefa (id, descricao, grupo, opcional, ajuste, etapa, produto) VALUES 
-- Prato 001: Bife (Etapa: 0=Hot, 1=Cold, 2=Assembly)
('T1_BIFE', 'Temperar o bife', '', 0, 0.00, 1, 'prato_001'),
('T2_BIFE', 'Grelhar: Ponto Médio (15 min)', 'medio', 0, 0.00, 0, 'prato_001'),
('T3_BIFE', 'Grelhar: Mal Passado (10 min)', 'mal_passado', 0, 0.00, 0, 'prato_001'),
('T4_BIFE', 'Grelhar: Bem Passado (20 min)', 'bem_passado', 0, 0.00, 0, 'prato_001'),
-- Prato 004: Burger
('T15_BUR', 'Tostar Pão de Hambúrguer', '', 0, 0.00, 0, 'prato_004'),
('T16_BUR', 'Grelhar Carne', 'normal', 0, 0.00, 0, 'prato_004'),
('T17_BUR', 'Grelhar Seitan', 'vegetariano', 0, 0.00, 0, 'prato_004');

-- 6. ALTERNATIVES & INGREDIENTS
INSERT OR IGNORE INTO tarefas_alternativas (principal, alternativa) VALUES 
('T2_BIFE', 'T3_BIFE'), ('T2_BIFE', 'T4_BIFE'), ('T16_BUR', 'T17_BUR');

INSERT OR IGNORE INTO tarefa_ingrediente (tarefa, ingrediente, quantidade) VALUES 
('T1_BIFE', 'flor_sal', 2), ('T1_BIFE', 'pimenta_preta', 1),
('T2_BIFE', 'bife_vazia', 1), ('T16_BUR', 'carne_picada', 150), ('T17_BUR', 'seitan', 150);

-- 7. SIMULATING A PEDIDO AND INSTANCE SELECTION
-- This simulates a real scenario where a user orders a Burger and picks Seitan
INSERT OR IGNORE INTO pedido (id, total, hora_pedido, estado) VALUES ('PED_01', 10.50, '2026-01-10 13:00:00', 1);

INSERT OR IGNORE INTO item_pedido (id, estado, quantidade, pedido, item) VALUES 
('IP_01', 0, 1, 'PED_01', 'prato_004');

-- Tracker for the first instance of the Burger
INSERT OR IGNORE INTO tarefas_por_instancia (posicao, item_pedido) VALUES (1, 'IP_01');

-- Selected tasks for this instance (e.g., Toasting and the Veggie choice)
INSERT OR IGNORE INTO tarefa_instancia_selecionada (item_pedido, posicao_instancia, tarefa, posicao_execucao, estado) VALUES 
('IP_01', 1, 'T15_BUR', 1, 0), -- Toasting
('IP_01', 1, 'T17_BUR', 2, 0); -- Chosen Seitan alternative
