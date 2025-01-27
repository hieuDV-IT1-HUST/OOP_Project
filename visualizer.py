import dash
from dash import html, dcc, Input, Output
import dash_cytoscape as cyto
import dash_bootstrap_components as dbc
import json
import numpy as np

# Tải dữ liệu từ file JSON
with open("output/PageRankPoints/pageRankPoints.json", "r") as file:
    data = json.load(file)

# Lọc bỏ các mục là tweet IDs và chuyển giá trị PageRank sang kiểu float
user_data = {k: float(v) for k, v in data.items() if not k.startswith("T")}

# Tính toán PageRank trung bình
average_pagerank = np.mean(list(user_data.values()))

# Tạo các node cho Cytoscape, chỉ giữ lại các node có PageRank lớn hơn trung bình
nodes = [
    {
        "data": {"id": user, "label": user, "pagerank": rank},
        "classes": "node",
    }
    for user, rank in user_data.items() if rank > average_pagerank
]

# Tạo các cạnh (edges) cho Cytoscape
edges = [
    {"data": {"source": source, "target": target}}
    for source, target in zip(list(user_data.keys())[:-1], list(user_data.keys())[1:])
    if source in [node["data"]["id"] for node in nodes]
    and target in [node["data"]["id"] for node in nodes]
]

# Khởi tạo ứng dụng Dash với Bootstrap
app = dash.Dash(__name__, external_stylesheets=[dbc.themes.BOOTSTRAP])
cyto.load_extra_layouts()

# Định nghĩa giao diện ứng dụng
app.layout = dbc.Container(
    fluid=True,
    children=[
        dbc.Row(
            dbc.Col(
                html.H1(
                    "Biểu đồ PageRank Tương tác",
                    className="text-center text-primary mb-4",
                ),
                width=12,
            )
        ),
        dbc.Row(
            [
                dbc.Col(
                    [
                        cyto.Cytoscape(
                            id="cytoscape",
                            elements=nodes + edges,
                            layout={
                                "name": "cose-bilkent",
                                "animate": True,
                                "animationDuration": 1000,
                                "nodeRepulsion": 4500,
                                "idealEdgeLength": 50,
                                "gravity": 0.25,
                            },
                            style={"width": "100%", "height": "600px", "border": "1px solid #ddd"},
                            stylesheet=[
                                {
                                    "selector": "node",
                                    "style": {
                                        "content": "data(label)",
                                        "font-size": "12px",
                                        "text-valign": "center",
                                        "text-halign": "center",
                                        "background-color": "mapData(pagerank, 0, 0.1, blue, red)",
                                        "width": "mapData(pagerank, 0, 0.1, 20, 80)",
                                        "height": "mapData(pagerank, 0, 0.1, 20, 80)",
                                    },
                                },
                                {
                                    "selector": "edge",
                                    "style": {
                                        "line-color": "#ccc",
                                        "width": 1,
                                    },
                                },
                                {
                                    "selector": ":selected",
                                    "style": {
                                        "background-color": "yellow",
                                        "line-color": "black",
                                        "target-arrow-color": "black",
                                        "source-arrow-color": "black",
                                        "width": 2,
                                    },
                                },
                            ],
                        ),
                        html.Div(id="cytoscape-mouseoverNodeData-output"),
                    ],
                    width=8,
                ),
                dbc.Col(
                    [
                        html.H5("Chi tiết Node", className="text-center"),
                        dbc.Card(
                            dbc.CardBody(
                                id="node-data",
                                className="text-center",
                                style={"fontSize": "16px", "minHeight": "150px"},
                            ),
                            style={"border": "1px solid #ddd"},
                        ),
                        html.P(
                            f"Ngưỡng (PageRank > Trung bình: {average_pagerank:.6f})",
                            className="text-muted text-center mt-3",
                        ),
                        html.Div(
                            [
                                html.Label("Chọn bố trí:"),
                                dcc.Dropdown(
                                    id="layout-dropdown",
                                    options=[
                                        {"label": "Cose-Bilkent", "value": "cose-bilkent"},
                                        {"label": "Concentric", "value": "concentric"},
                                        {"label": "Breadthfirst", "value": "breadthfirst"},
                                        {"label": "Grid", "value": "grid"},
                                    ],
                                    value="cose-bilkent",
                                    clearable=False,
                                ),
                            ],
                            className="mt-4",
                        ),
                    ],
                    width=4,
                ),
            ]
        ),
        dbc.Row(
            dbc.Col(
                html.Footer(
                    "© 2024 Biểu đồ Tương tác. Powered by Dash & Cytoscape.",
                    className="text-center mt-4",
                )
            )
        ),
    ],
)

# Callback để hiển thị dữ liệu node khi được click
@app.callback(
    Output("node-data", "children"),
    Input("cytoscape", "tapNodeData"),
)
def display_node_data(data):
    if data:
        return f"Bạn đã click vào: {data['label']} (PageRank: {data['pagerank']:.6f})"
    return "Click vào một node để xem chi tiết."

# Callback để thay đổi bố trí của biểu đồ dựa trên lựa chọn của người dùng
@app.callback(
    Output("cytoscape", "layout"),
    Input("layout-dropdown", "value"),
)
def update_layout(layout):
    return {"name": layout}

# Callback để hiển thị thông tin node khi di chuột qua
@app.callback(
    Output("cytoscape-mouseoverNodeData-output", "children"),
    Input("cytoscape", "mouseoverNodeData"),
)
def display_mouseover_node_data(data):
    if data:
        return f"Đang di chuột qua: {data['label']} (PageRank: {data['pagerank']:.6f})"
    return "Di chuột qua một node để xem chi tiết."

if __name__ == "__main__":
    app.run_server(debug=True)