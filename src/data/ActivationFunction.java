package data;

public enum ActivationFunction {

	LINEAR {
		@Override
		public float applyActivation(float value) {
			return value;
		}
	},
	SIGMOID {
		@Override
		public float applyActivation(float value) {
			return (float)(1 / (1 + Math.exp(-value)));
		}
	},
	TANH {
		@Override
		public float applyActivation(float value) {
			return (float)Math.tanh(value);
		}
	},
	STEP {
		@Override
		public float applyActivation(float value) {
			if (value <= 0) {
				return 0;				
			}
			return 1;
		}
	};
	
	public abstract float applyActivation(float value);
	
}
