export default function BudgetList({ budgets, onEdit, onReload, onDelete }) {

  const tableStyle = {
    width: "100%",
    borderCollapse: "collapse",
    fontSize: "14px",
  };

  const thStyle = {
    backgroundColor: "#1976D2",
    color: "#fff",
    padding: "10px",
    textAlign: "left",
    borderBottom: "2px solid #ddd",
  };

  const tdStyle = {
    padding: "10px",
    borderBottom: "1px solid #eee",
  };

  const actionButtonStyle = {
    padding: "6px 12px",
    marginRight: "6px",
    border: "none",
    borderRadius: "6px",
    cursor: "pointer",
    fontSize: "12px",
  };

  const editButtonStyle = { ...actionButtonStyle, backgroundColor: "#FF9800", color: "#fff" };
  const deleteButtonStyle = { ...actionButtonStyle, backgroundColor: "#f44336", color: "#fff" };

  return (
    <div>
      <h2>예산 목록</h2>
      <table style={tableStyle}>
        <thead>
          <tr>
            <th style={thStyle}>카테고리</th>
            <th style={thStyle}>금액</th>
            <th style={thStyle}>설명</th>
            <th style={{ ...thStyle, width: "120px" }}></th>
          </tr>
        </thead>
        <tbody>
          {budgets.map((b) => (
            <tr key={b.id}>
              <td style={tdStyle}>{b.categoryDescription}</td>
              <td style={tdStyle}>{b.amount}원</td>
              <td style={tdStyle}>{b.budgetDescription}</td>
              <td style={tdStyle}>
                <button style={editButtonStyle} onClick={() => onEdit(b)}>수정</button>
                <button style={deleteButtonStyle} onClick={() => onDelete(b.id)}>삭제</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
