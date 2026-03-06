import pickle
import numpy as np
import pandas as pd
from sklearn.preprocessing import StandardScaler, LabelEncoder
from sklearn.ensemble import GradientBoostingRegressor

# 添加XGBoostModel类定义，与训练脚本中的定义保持一致
class XGBoostModel:
    def __init__(self, n_estimators=10, learning_rate=0.1, max_depth=1):
        self.model = GradientBoostingRegressor(
            n_estimators=n_estimators,
            learning_rate=learning_rate,
            max_depth=max_depth,
            subsample=0.8,  # 添加子采样防止过拟合
            max_features='sqrt'  # 特征采样增强随机性
        )

    def fit(self, X_train, y_train):
        self.model.fit(X_train, y_train.ravel())

    def predict(self, X_test):
        return self.model.predict(X_test)

class XGBoostInference:
    def __init__(self, model_path='models/xgboost_model.pkl', scaler_x_path='models/xgboost_scaler_x.pkl', scaler_y_path='models/xgboost_scaler_y.pkl'):
        """
        初始化XGBoost推理模型
        
        Args:
            model_path: XGBoost模型文件路径
            scaler_x_path: 特征标准化器文件路径
            scaler_y_path: 目标标准化器文件路径
        """
        # 加载模型
        with open(model_path, 'rb') as f:
            self.model = pickle.load(f)
        
        # 尝试加载标准化器，如果不存在则在预测时创建
        try:
            with open(scaler_x_path, 'rb') as f:
                self.scaler_X = pickle.load(f)
            
            with open(scaler_y_path, 'rb') as f:
                self.scaler_y = pickle.load(f)
            self.scalers_loaded = True
        except FileNotFoundError:
            print("警告：标准化器文件未找到，将使用新的标准化器")
            self.scaler_X = StandardScaler()
            self.scaler_y = StandardScaler()
            self.scalers_loaded = False
        
        # 初始化标签编码器
        self.le = LabelEncoder()
        # 特征列表（与训练时保持一致）
        self.features = [
            '左电仓SOC', '右电仓SOC', '左电仓电流', '左电仓电压', '左推进器功率', '左推进器转速',
            '右电仓电流', '右电仓电压', '右推进器功率', '右推进器转速',
            '风速', '风向', '流速', '流向', '对地航速（海里）', '单位距离能量消耗（kWh/海里）', '工作状态'
        ]
        self.time_steps = 10
    
    def preprocess_data(self, data, fit_scalers=False):
        """
        预处理输入数据
        
        Args:
            data: 输入数据，pandas DataFrame
            fit_scalers: 是否拟合标准化器（当使用新数据时设为True）
            
        Returns:
            处理后的特征数据，准备用于预测
        """
        # 复制数据以避免修改原始数据
        processed_data = data.copy()
        
        # 删除缺失值
        processed_data = processed_data.dropna()
        
        # 确保数据长度足够创建时间序列
        if len(processed_data) < self.time_steps:
            raise ValueError(f"输入数据长度必须至少为{self.time_steps}个时间步")
        
        # 标签编码工作状态
        # 注意：这里假设工作状态的值与训练时相同
        # 如果遇到新的工作状态值，可能需要特殊处理
        if '工作状态' in processed_data.columns:
            # 处理编码：如果是字符串类型则编码，如果已经是数字则跳过
            if processed_data['工作状态'].dtype == 'object':
                # 使用try-except处理可能的新类别
                try:
                    processed_data['工作状态'] = self.le.fit_transform(processed_data['工作状态'])
                except Exception as e:
                    print(f"工作状态编码警告: {e}")
        
        # 确保所有特征列都存在
        missing_features = set(self.features) - set(processed_data.columns)
        if missing_features:
            raise ValueError(f"缺少必要的特征列: {missing_features}")
        
        # 选择特征列
        X = processed_data[self.features].values
        
        # 创建时间序列数据 - 先创建时间序列
        X_sequences = []
        indices = []
        
        for i in range(len(X) - self.time_steps + 1):
            X_sequences.append(X[i:i + self.time_steps])
            indices.append(processed_data.index[i + self.time_steps - 1])
        
        # 展平时间步维度 - 然后展平
        X_flattened = np.array(X_sequences).reshape(len(X_sequences), -1)
        
        # 标准化特征 - 最后在展平的数据上进行标准化
        if fit_scalers or not self.scalers_loaded:
            X_scaled = self.scaler_X.fit_transform(X_flattened)
        else:
            try:
                X_scaled = self.scaler_X.transform(X_flattened)
            except ValueError as e:
                print(f"标准化错误: {e}")
                print(f"期望的特征数: {self.scaler_X.n_features_in_}, 实际特征数: {X_flattened.shape[1]}")
                # 如果特征数不匹配，尝试重新拟合标准化器
                print("尝试重新拟合标准化器...")
                X_scaled = self.scaler_X.fit_transform(X_flattened)
        
        return X_scaled, indices
    
    def predict(self, data, fit_scalers=False, inverse_transform=True):
        """
        使用模型进行预测
        
        Args:
            data: 输入数据，pandas DataFrame
            fit_scalers: 是否拟合标准化器
            inverse_transform: 是否对预测结果进行逆标准化
            
        Returns:
            预测结果和对应的索引
        """
        # 预处理数据
        X_processed, indices = self.preprocess_data(data, fit_scalers)
        
        # 进行预测
        predictions = self.model.predict(X_processed)
        
        # 逆标准化
        if inverse_transform and (fit_scalers or self.scalers_loaded):
            # 重塑为二维数组以进行逆变换
            predictions_2d = predictions.reshape(-1, 1)
            if fit_scalers or not self.scalers_loaded:
                # 如果是新数据，先模拟拟合scaler_y
                # 注意：这可能不准确，建议使用与训练时相同的scaler
                dummy_y = np.random.random((10, 1))  # 创建虚拟数据
                self.scaler_y.fit(dummy_y)
            predictions = self.scaler_y.inverse_transform(predictions_2d).flatten()
        
        return predictions, indices
    
    def predict_single_sequence(self, sequence_data, fit_scalers=False, inverse_transform=True):
        """
        预测单个序列（长度为time_steps的数据）
        
        Args:
            sequence_data: 单个序列数据，pandas DataFrame，长度为time_steps
            fit_scalers: 是否拟合标准化器
            inverse_transform: 是否对预测结果进行逆标准化
            
        Returns:
            单个预测结果
        """
        if len(sequence_data) != self.time_steps:
            raise ValueError(f"输入序列长度必须为{self.time_steps}")
        
        # 预处理数据
        X_processed, _ = self.preprocess_data(sequence_data, fit_scalers)
        
        # 确保只有一个样本
        if len(X_processed) != 1:
            raise ValueError("处理后的数据应只包含一个样本")
        
        # 进行预测
        prediction = self.model.predict(X_processed)[0]
        
        # 逆标准化
        if inverse_transform and (fit_scalers or self.scalers_loaded):
            prediction_2d = np.array(prediction).reshape(-1, 1)
            if fit_scalers or not self.scalers_loaded:
                # 如果是新数据，先模拟拟合scaler_y
                dummy_y = np.random.random((10, 1))
                self.scaler_y.fit(dummy_y)
            prediction = self.scaler_y.inverse_transform(prediction_2d).flatten()[0]
        
        return prediction

# 示例用法
def example_usage():
    # 创建推理实例
    inference = XGBoostInference()
    
    # 示例1：从Excel文件加载数据并进行预测
    try:
        # 加载数据
        data = pd.read_excel('longest_continuous_period.xlsx')
        
        # 进行预测
        predictions, indices = inference.predict(data)
        
        print(f"预测结果数量: {len(predictions)}")
        print(f"前5个预测结果: {predictions[:5]}")
        
        # 将结果保存到DataFrame
        result_df = pd.DataFrame({
            '索引': indices,
            '预测值': predictions
        })
        result_df.to_csv('xgboost_predictions_result.csv', index=False)
        print("预测结果已保存到 xgboost_predictions_result.csv")
        
    except Exception as e:
        print(f"示例运行错误: {e}")
    
    # 示例2：预测单个序列
    print("\n--- 单个序列预测示例 ---")
    try:
        # 创建一个示例序列（这里仅作演示，实际应使用真实数据）
        sample_data = pd.DataFrame({
            '左电仓SOC': [85.5] * 10,
            '右电仓SOC': [86.2] * 10,
            '左电仓电流': [120.3] * 10,
            '左电仓电压': [380.5] * 10,
            '左推进器功率': [45.2] * 10,
            '左推进器转速': [1200] * 10,
            '右电仓电流': [118.7] * 10,
            '右电仓电压': [379.8] * 10,
            '右推进器功率': [44.8] * 10,
            '右推进器转速': [1180] * 10,
            '风速': [12.5] * 10,
            '风向': [90] * 10,
            '流速': [2.3] * 10,
            '流向': [180] * 10,
            '对地航速（海里）': [8.5] * 10,
            '单位距离能量消耗（kWh/海里）': [5.2] * 10,
            '工作状态': ['巡航'] * 10  # 假设工作状态为巡航
        })
        
        # 预测单个序列
        single_prediction = inference.predict_single_sequence(sample_data, fit_scalers=True)
        print(f"单个序列预测结果: {single_prediction}")
        
    except Exception as e:
        print(f"单个序列预测错误: {e}")

if __name__ == "__main__":
    # 运行示例
    print("XGBoost模型推理脚本")
    print("=" * 50)
    example_usage()