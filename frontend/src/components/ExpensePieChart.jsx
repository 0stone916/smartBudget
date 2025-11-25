import React from "react";
import { Pie } from "react-chartjs-2";
import { Chart, ArcElement, Tooltip, Legend } from "chart.js";

Chart.register(ArcElement, Tooltip, Legend);

// 카테고리별 고정 색상 팔레트
const CATEGORY_COLORS = [
  "#4E79A7", "#F28E2B", "#E15759", "#76B7B2", "#59A14F",
  "#EDC948", "#B07AA1", "#FF9DA7", "#9C755F", "#BAB0AC"
];

export default function ExpensePieChart({ budgets, expenses }) {
  if (!budgets || budgets.length === 0) return <div>예산이 없습니다.</div>;

  const containerStyle = {
    display: "flex",
    flexWrap: "wrap",
    gap: "30px",
    marginTop: "30px",
  };

  const boxStyle = {
    width: "250px",
    textAlign: "center",
  };

  return (
    <div>
      <div style={containerStyle}>
        {budgets.map((b, index) => {
          const limit = Number(b.amount ?? 0);
          const categoryCode = b.categoryDescription;

          // 지출 합산
          const spent = expenses
            .filter((e) => e.category === categoryCode)
            .reduce((sum, e) => sum + Number(e.amount ?? 0), 0);

          const remaining = Math.max(limit - spent, 0);

          // 카테고리 색 하나 고정 선택
          const color = CATEGORY_COLORS[index % CATEGORY_COLORS.length];

          const data = {
            labels: ["지출", "잔여"],
            datasets: [
              {
                data: [spent > limit ? limit : spent, remaining],
                backgroundColor: [
                  color,
                  "#E0E0E0"
                ],
                hoverBackgroundColor: [
                  color,
                  "#D0D0D0"
                ],
                borderWidth: 1,
              },
            ],
          };

          return (
            <div key={b.id} style={boxStyle}>
              {/* 초과 시 빨간색 강조 */}
              <h3>
                {categoryCode} {spent > limit && <span style={{ color: "red" }}>초과!</span>}
              </h3>
              <Pie data={data} />
              <div style={{ marginTop: "10px" }}>
                지출 {spent.toLocaleString()} / 예산 {limit.toLocaleString()}{" "}<br></br>
                {spent > limit && <span style={{ color: "red" }}>(초과 {spent - limit})</span>}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
