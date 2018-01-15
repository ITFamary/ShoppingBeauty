CREATE OR REPLACE VIEW `CapitalFlow`
AS
  SELECT
    concat('RL', r.ID) AS `ID`,
    r.USER_ID          AS `USER_ID`,
    r.ID               AS `ORDER_ID`,
    r.CREATETIME       AS `HAPPEN_TIME`,
    0                  AS `TYPE`,
    r.AMOUNT           AS `CHANGED`
  FROM RechargeLog AS r
  UNION
  SELECT
    concat('RL', o.ORDERID) AS `ID`,
    o.PAYER_ID              AS `USER_ID`,
    o.ORDERID               AS `ORDER_ID`,
    o.PAYTIME               AS `HAPPEN_TIME`,
    1                       AS `TYPE`,
    -o.FINALAMOUNT          AS `CHANGED`
  FROM MainOrder AS o
  WHERE ORDERSTATUS = 2