const express = require("express");
const router = express.Router();
const { pool } = require('../db/pool');

// 체질별 랜덤 건강 제품 추천
router.get("/products/:type", async (req, res) => {
  const { type } = req.params;

  try {
    // constitution_products 테이블에서 랜덤 1개 가져오기
    const [rows] = await pool.query(
      "SELECT product_name, coupang_link FROM constitution_products WHERE constitution_type = ? ORDER BY RAND() LIMIT 1",
      [type]
    );

    if (rows.length === 0) {
      return res.status(404).json({ message: "해당 체질에 맞는 제품이 없습니다." });
    }

    const product = rows[0];
    res.status(200).json({
      constitution_type: type,
      recommended_product: product.product_name,
      coupang_link: product.coupang_link,
    });
  } catch (error) {
    console.error(" DB 조회 오류:", error);
    res.status(500).json({ error: "서버 내부 오류 발생" });
  }
});

module.exports = router;
