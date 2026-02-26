import React from "react";
import { Pie } from "react-chartjs-2";
import { Chart, ArcElement, Tooltip, Legend } from "chart.js";

Chart.register(ArcElement, Tooltip, Legend);

const CATEGORY_COLORS = [
  "#4E79A7", "#F28E2B", "#E15759", "#76B7B2", "#59A14F",
  "#EDC948", "#B07AA1", "#FF9DA7", "#9C755F", "#BAB0AC"
];

// expenses 대신 백엔드에서 집계해준 statistics(카테고리별 합계)를 받습니다.
export default function ExpensePieChart({ budgets, statistics }) {
  // 데이터 방어 코드 (에러 방지 핵심)
  if (!budgets || budgets.length === 0) return <div>등록된 예산이 없습니다.</div>;
  if (!statistics) return <div>지출 통계를 불러오는 중...</div>;

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
    <div style={containerStyle}>
      {budgets.map((b, index) => {
        const limit = Number(b.amount ?? 0);
        const categoryCode = b.category.code;

        // [핵심 변경] 400만 건을 filter하는 대신, 집계된 statistics에서 해당 카테고리 값만 찾음
        const stat = statistics.find((s) => s.code === categoryCode);
        const spent = Number(stat?.totalAmount ?? 0);

        const remaining = Math.max(limit - spent, 0);
        const color = CATEGORY_COLORS[index % CATEGORY_COLORS.length];

        const data = {
          labels: ["지출", "잔여"],
          datasets: [
            {
              // 초과 시에도 차트 모양 유지를 위해 spent가 limit을 넘지 않게 처리
              data: [spent > limit ? limit : spent, remaining],
              backgroundColor: [color, "#E0E0E0"],
              hoverBackgroundColor: [color, "#D0D0D0"],
              borderWidth: 1,
            },
          ],
        };

        return (
          <div key={b.id} style={boxStyle}>
            <h3>
              {categoryCode} {spent > limit && <span style={{ color: "red" }}>초과!</span>}
            </h3>
            <Pie data={data} />
            <div style={{ marginTop: "10px" }}>
              지출 {spent.toLocaleString()} / 예산 {limit.toLocaleString()}<br />
              {spent > limit && (
                <span style={{ color: "red" }}>(초과 {(spent - limit).toLocaleString()})</span>
              )}
            </div>
          </div>
        );
      })}
    </div>
  );
}