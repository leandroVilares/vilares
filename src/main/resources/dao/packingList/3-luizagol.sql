SELECT
A.CODENTVEIC NROMANEIO,
A.CODFIL,
'LuizaGol' TIPO,
A.DTROMANEIO DATA_ROMANEIO,
A.CODVEIC CODVEIC,
B.NUMPEDVEN,
C.NUMLOTE NLOTE
 FROM USR_PORTARIACD.MAG_T_PRT_MOV_VEICULO A,
 USR_PORTARIACD.MAG_T_PRT_HIST_CROSSDOCKING B,
 GEMCO.MOV_PEDIDO C
WHERE A.CODFIL = B.CODFIL
  AND A.CODENTVEIC = B.CODENTVEIC
  AND B.CODFILNF = C.FILORIG
  AND B.NUMPEDVEN=C.NUMPEDVEN
  AND B.CODPROCESSO=0
  [:CODFIL][AND A.CODFIL = :CODFIL];
  [:NROMANEIO][AND A.CODENTVEIC = :NROMANEIO];
  [:START_DATE][AND A.DTROMANEIO >= :START_DATE];
  [:END_DATE][AND A.DTROMANEIO <= :END_DATE];

