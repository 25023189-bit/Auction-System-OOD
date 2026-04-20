import numpy as np
import matplotlib.pyplot as plt
from sklearn.linear_model import LogisticRegression

# =========================
# 1. Tạo dữ liệu (2 feature)
# =========================
# class 0
X0 = np.array([
    [1, 2],
    [2, 1],
    [2, 2],
    [3, 1]
])

# class 1
X1 = np.array([
    [4, 5],
    [5, 4],
    [5, 5],
    [6, 4]
])

X = np.vstack((X0, X1))
y = np.array([0]*len(X0) + [1]*len(X1))

# =========================
# 2. Train Logistic Regression
# =========================
model = LogisticRegression(C=1000)
model.fit(X, y)

# Lấy weight
w = model.coef_[0]
b = model.intercept_[0]

print("w:", w)
print("b:", b)

# =========================
# 3. Vẽ dữ liệu
# =========================
plt.scatter(X0[:,0], X0[:,1], label="Class 0")
plt.scatter(X1[:,0], X1[:,1], label="Class 1")

# =========================
# 4. Vẽ decision boundary
# =========================
# w1*x1 + w2*x2 + b = 0
# => x2 = -(w1*x1 + b)/w2

x1_vals = np.linspace(0, 7, 100)
x2_vals = -(w[0]*x1_vals + b) / w[1]

plt.plot(x1_vals, x2_vals, linestyle="--")

# =========================
# 5. Trang trí
# =========================
plt.xlabel("Feature 1")
plt.ylabel("Feature 2")
plt.title("Decision Boundary - Logistic Regression")
plt.legend()
plt.grid()

plt.show()