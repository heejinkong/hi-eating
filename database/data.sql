MERGE INTO users u
USING (
    SELECT
        'user@greenfood.test' AS email,
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy' AS password,
        '일반 사용자' AS name,
        'FEMALE' AS gender,
        DATE '1995-05-20' AS birth
    FROM dual
) s
ON (u.email = s.email)
WHEN NOT MATCHED THEN
    INSERT (email, password, name, gender, birth, created_at)
    VALUES (s.email, s.password, s.name, s.gender, s.birth, SYSTIMESTAMP);

MERGE INTO users u
USING (
    SELECT
        'admin@greenfood.test' AS email,
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy' AS password,
        '관리자' AS name,
        'MALE' AS gender,
        DATE '1990-01-10' AS birth
    FROM dual
) s
ON (u.email = s.email)
WHEN NOT MATCHED THEN
    INSERT (email, password, name, gender, birth, created_at)
    VALUES (s.email, s.password, s.name, s.gender, s.birth, SYSTIMESTAMP);

MERGE INTO user_auth ua
USING (
    SELECT u.id AS user_id, 'ROLE_USER' AS auth
    FROM users u
    WHERE u.email = 'user@greenfood.test'
) s
ON (ua.user_id = s.user_id AND ua.auth = s.auth)
WHEN NOT MATCHED THEN
    INSERT (user_id, auth, created_at)
    VALUES (s.user_id, s.auth, SYSTIMESTAMP);

MERGE INTO user_auth ua
USING (
    SELECT u.id AS user_id, 'ROLE_ADMIN' AS auth
    FROM users u
    WHERE u.email = 'admin@greenfood.test'
) s
ON (ua.user_id = s.user_id AND ua.auth = s.auth)
WHEN NOT MATCHED THEN
    INSERT (user_id, auth, created_at)
    VALUES (s.user_id, s.auth, SYSTIMESTAMP);

MERGE INTO categories c
USING (SELECT '유기농 채소' AS name FROM dual) s
ON (c.name = s.name)
WHEN NOT MATCHED THEN
    INSERT (name, created_at)
    VALUES (s.name, SYSTIMESTAMP);

MERGE INTO categories c
USING (SELECT '제철 과일' AS name FROM dual) s
ON (c.name = s.name)
WHEN NOT MATCHED THEN
    INSERT (name, created_at)
    VALUES (s.name, SYSTIMESTAMP);

MERGE INTO categories c
USING (SELECT '쌀/잡곡' AS name FROM dual) s
ON (c.name = s.name)
WHEN NOT MATCHED THEN
    INSERT (name, created_at)
    VALUES (s.name, SYSTIMESTAMP);

MERGE INTO categories c
USING (SELECT '정육/계란' AS name FROM dual) s
ON (c.name = s.name)
WHEN NOT MATCHED THEN
    INSERT (name, created_at)
    VALUES (s.name, SYSTIMESTAMP);

MERGE INTO categories c
USING (SELECT '수산/해산' AS name FROM dual) s
ON (c.name = s.name)
WHEN NOT MATCHED THEN
    INSERT (name, created_at)
    VALUES (s.name, SYSTIMESTAMP);

MERGE INTO categories c
USING (SELECT '건강 간편식' AS name FROM dual) s
ON (c.name = s.name)
WHEN NOT MATCHED THEN
    INSERT (name, created_at)
    VALUES (s.name, SYSTIMESTAMP);

MERGE INTO categories c
USING (SELECT '샐러드/밀키트' AS name FROM dual) s
ON (c.name = s.name)
WHEN NOT MATCHED THEN
    INSERT (name, created_at)
    VALUES (s.name, SYSTIMESTAMP);

MERGE INTO categories c
USING (SELECT '음료/차' AS name FROM dual) s
ON (c.name = s.name)
WHEN NOT MATCHED THEN
    INSERT (name, created_at)
    VALUES (s.name, SYSTIMESTAMP);

MERGE INTO categories c
USING (SELECT '간식/디저트' AS name FROM dual) s
ON (c.name = s.name)
WHEN NOT MATCHED THEN
    INSERT (name, created_at)
    VALUES (s.name, SYSTIMESTAMP);

MERGE INTO categories c
USING (SELECT '양념/소스' AS name FROM dual) s
ON (c.name = s.name)
WHEN NOT MATCHED THEN
    INSERT (name, created_at)
    VALUES (s.name, SYSTIMESTAMP);

MERGE INTO promotions p
USING (
    SELECT
        '그린푸드 오픈 기획전' AS title,
        '/images/promotions/greenfood-open.png' AS img_src,
        '/products' AS link,
        1000 AS display_order,
        SYSTIMESTAMP AS starts_at,
        SYSTIMESTAMP + INTERVAL '30' DAY AS ends_at
    FROM dual
) s
ON (p.display_order = s.display_order)
WHEN NOT MATCHED THEN
    INSERT (title, img_src, link, display_order, starts_at, ends_at, created_at)
    VALUES (s.title, s.img_src, s.link, s.display_order, s.starts_at, s.ends_at, SYSTIMESTAMP);

MERGE INTO promotions p
USING (
    SELECT
        '제철 신선식품 특가' AS title,
        '/images/promotions/seasonal-fresh.png' AS img_src,
        '/products?sort=latest' AS link,
        2000 AS display_order,
        SYSTIMESTAMP AS starts_at,
        SYSTIMESTAMP + INTERVAL '30' DAY AS ends_at
    FROM dual
) s
ON (p.display_order = s.display_order)
WHEN NOT MATCHED THEN
    INSERT (title, img_src, link, display_order, starts_at, ends_at, created_at)
    VALUES (s.title, s.img_src, s.link, s.display_order, s.starts_at, s.ends_at, SYSTIMESTAMP);

MERGE INTO promotions p
USING (
    SELECT
        '건강한 한 끼 밀키트' AS title,
        '/images/promotions/healthy-mealkit.png' AS img_src,
        '/products?category=meal-kit' AS link,
        3000 AS display_order,
        SYSTIMESTAMP AS starts_at,
        SYSTIMESTAMP + INTERVAL '30' DAY AS ends_at
    FROM dual
) s
ON (p.display_order = s.display_order)
WHEN NOT MATCHED THEN
    INSERT (title, img_src, link, display_order, starts_at, ends_at, created_at)
    VALUES (s.title, s.img_src, s.link, s.display_order, s.starts_at, s.ends_at, SYSTIMESTAMP);

MERGE INTO promotions p
USING (
    SELECT
        '친환경 장보기 추천' AS title,
        '/images/promotions/eco-market.png' AS img_src,
        '/products?category=organic' AS link,
        4000 AS display_order,
        SYSTIMESTAMP AS starts_at,
        SYSTIMESTAMP + INTERVAL '30' DAY AS ends_at
    FROM dual
) s
ON (p.display_order = s.display_order)
WHEN NOT MATCHED THEN
    INSERT (title, img_src, link, display_order, starts_at, ends_at, created_at)
    VALUES (s.title, s.img_src, s.link, s.display_order, s.starts_at, s.ends_at, SYSTIMESTAMP);


-- PHASE2_CURRY_TARGET_SELECTION_DEMO_START
-- 실제 상품 P-4 카레와 P-5 잡채 기준 대상 선정 데이터.

MERGE INTO users u
USING (
    SELECT
        'ai-curry-high@greenfood.test' AS email,
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy' AS password,
        '카레 고관심 사용자' AS name,
        'FEMALE' AS gender,
        DATE '1992-03-15' AS birth
    FROM dual
) s
ON (u.email = s.email)
WHEN MATCHED THEN
    UPDATE SET u.deleted_at = NULL, u.updated_at = SYSTIMESTAMP
WHEN NOT MATCHED THEN
    INSERT (email, password, name, gender, birth, created_at)
    VALUES (s.email, s.password, s.name, s.gender, s.birth, SYSTIMESTAMP);

MERGE INTO users u
USING (
    SELECT
        'ai-curry-selected@greenfood.test' AS email,
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy' AS password,
        '카레 선정 사용자' AS name,
        'MALE' AS gender,
        DATE '1988-09-21' AS birth
    FROM dual
) s
ON (u.email = s.email)
WHEN MATCHED THEN
    UPDATE SET u.deleted_at = NULL, u.updated_at = SYSTIMESTAMP
WHEN NOT MATCHED THEN
    INSERT (email, password, name, gender, birth, created_at)
    VALUES (s.email, s.password, s.name, s.gender, s.birth, SYSTIMESTAMP);

MERGE INTO users u
USING (
    SELECT
        'ai-curry-medium@greenfood.test' AS email,
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy' AS password,
        '카레 중관심 사용자' AS name,
        'OTHER' AS gender,
        DATE '1997-01-08' AS birth
    FROM dual
) s
ON (u.email = s.email)
WHEN MATCHED THEN
    UPDATE SET u.deleted_at = NULL, u.updated_at = SYSTIMESTAMP
WHEN NOT MATCHED THEN
    INSERT (email, password, name, gender, birth, created_at)
    VALUES (s.email, s.password, s.name, s.gender, s.birth, SYSTIMESTAMP);

MERGE INTO users u
USING (
    SELECT
        'ai-curry-low@greenfood.test' AS email,
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy' AS password,
        '카레 저관심 사용자' AS name,
        'FEMALE' AS gender,
        DATE '2001-11-30' AS birth
    FROM dual
) s
ON (u.email = s.email)
WHEN MATCHED THEN
    UPDATE SET u.deleted_at = NULL, u.updated_at = SYSTIMESTAMP
WHEN NOT MATCHED THEN
    INSERT (email, password, name, gender, birth, created_at)
    VALUES (s.email, s.password, s.name, s.gender, s.birth, SYSTIMESTAMP);

INSERT ALL
    INTO purchases (user_id, product_id, quantity, purchase_price, created_at)
        VALUES (user_id, product_id, 1, product_price, SYSTIMESTAMP - INTERVAL '5' DAY)
    INTO purchases (user_id, product_id, quantity, purchase_price, created_at)
        VALUES (user_id, product_id, 1, product_price, SYSTIMESTAMP - INTERVAL '15' DAY)
    INTO purchases (user_id, product_id, quantity, purchase_price, created_at)
        VALUES (user_id, product_id, 2, product_price, SYSTIMESTAMP - INTERVAL '25' DAY)
    INTO purchases (user_id, product_id, quantity, purchase_price, created_at)
        VALUES (user_id, product_id, 1, product_price, SYSTIMESTAMP - INTERVAL '35' DAY)
SELECT
    u.id AS user_id,
    p.id AS product_id,
    p.price AS product_price
FROM users u
JOIN products p ON p.name IN (
    '[소프트프로틴] 고기듬뿍 일본식카레 (1인분) 150g',
    'NEW 버섯 듬뿍 잡채 353g'
)
WHERE u.email = 'ai-curry-high@greenfood.test'
  AND NOT EXISTS (
      SELECT 1 FROM purchases existing
      WHERE existing.user_id = u.id AND existing.product_id = p.id
  );

INSERT ALL
    INTO purchases (user_id, product_id, quantity, purchase_price, created_at)
        VALUES (user_id, product_id, 1, product_price, SYSTIMESTAMP - INTERVAL '7' DAY)
    INTO purchases (user_id, product_id, quantity, purchase_price, created_at)
        VALUES (user_id, product_id, 1, product_price, SYSTIMESTAMP - INTERVAL '20' DAY)
SELECT
    u.id AS user_id,
    p.id AS product_id,
    p.price AS product_price
FROM users u
JOIN products p ON p.name IN (
    '[소프트프로틴] 고기듬뿍 일본식카레 (1인분) 150g',
    'NEW 버섯 듬뿍 잡채 353g'
)
WHERE u.email = 'ai-curry-selected@greenfood.test'
  AND NOT EXISTS (
      SELECT 1 FROM purchases existing
      WHERE existing.user_id = u.id AND existing.product_id = p.id
  );

INSERT INTO purchases (user_id, product_id, quantity, purchase_price, created_at)
SELECT
    u.id,
    p.id,
    1,
    p.price,
    SYSTIMESTAMP - INTERVAL '45' DAY
FROM users u
JOIN products p ON p.name IN (
    '[소프트프로틴] 고기듬뿍 일본식카레 (1인분) 150g',
    'NEW 버섯 듬뿍 잡채 353g'
)
WHERE u.email = 'ai-curry-medium@greenfood.test'
  AND NOT EXISTS (
      SELECT 1 FROM purchases existing
      WHERE existing.user_id = u.id AND existing.product_id = p.id
  );

MERGE INTO favorites f
USING (
    SELECT u.id AS user_id, p.id AS product_id
    FROM users u
    CROSS JOIN products p
    WHERE u.email IN (
        'ai-curry-high@greenfood.test',
        'ai-curry-selected@greenfood.test',
        'ai-curry-medium@greenfood.test',
        'ai-curry-low@greenfood.test'
    )
      AND p.name IN (
          '[소프트프로틴] 고기듬뿍 일본식카레 (1인분) 150g',
          'NEW 버섯 듬뿍 잡채 353g'
      )
) s
ON (f.user_id = s.user_id AND f.product_id = s.product_id)
WHEN NOT MATCHED THEN
    INSERT (user_id, product_id, created_at)
    VALUES (s.user_id, s.product_id, SYSTIMESTAMP);

INSERT INTO reviews (user_id, product_id, purchase_id, rating, content, created_at)
SELECT
    u.id,
    p.id,
    MIN(pu.id),
    5,
    '카레 고관심 사용자 리뷰',
    SYSTIMESTAMP - INTERVAL '3' DAY
FROM users u
JOIN products p ON p.name IN (
    '[소프트프로틴] 고기듬뿍 일본식카레 (1인분) 150g',
    'NEW 버섯 듬뿍 잡채 353g'
)
JOIN purchases pu ON pu.user_id = u.id AND pu.product_id = p.id
WHERE u.email = 'ai-curry-high@greenfood.test'
  AND NOT EXISTS (
      SELECT 1 FROM reviews existing
      WHERE existing.user_id = u.id AND existing.product_id = p.id
  )
GROUP BY u.id, p.id;

INSERT INTO reviews (user_id, product_id, purchase_id, rating, content, created_at)
SELECT
    u.id,
    p.id,
    MIN(pu.id),
    4,
    '카레 선정 사용자 리뷰',
    SYSTIMESTAMP - INTERVAL '6' DAY
FROM users u
JOIN products p ON p.name IN (
    '[소프트프로틴] 고기듬뿍 일본식카레 (1인분) 150g',
    'NEW 버섯 듬뿍 잡채 353g'
)
JOIN purchases pu ON pu.user_id = u.id AND pu.product_id = p.id
WHERE u.email = 'ai-curry-selected@greenfood.test'
  AND NOT EXISTS (
      SELECT 1 FROM reviews existing
      WHERE existing.user_id = u.id AND existing.product_id = p.id
  )
GROUP BY u.id, p.id;

-- PHASE2_CURRY_TARGET_SELECTION_DEMO_END

COMMIT;
