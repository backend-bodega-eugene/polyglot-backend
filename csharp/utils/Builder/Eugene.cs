namespace Utils.Builder;

public sealed class Eugene
{
    public string Name { get; }
    public int Age { get; }
    public int BreathsPerMinute { get; }

    private Eugene(string name, int age, int breathsPerMinute)
    {
        Name = name;
        Age = age;
        BreathsPerMinute = breathsPerMinute;
    }

    public static Builder New() => new Builder();

    public sealed class Builder
    {
        private string _name = "laohu";
        private int _age = 18;
        private int _breathsPerMinute = 20;

        public Builder WithName(string name) { _name = name; return this; }
        public Builder WithAge(int age) { _age = age; return this; }
        public Builder WithBreathsPerMinute(int bpm) { _breathsPerMinute = bpm; return this; }

        // 从已有对象克隆到 Builder，再微调
        public Builder CloneFrom(Eugene e)
        {
            _name = e.Name;
            _age = e.Age;
            _breathsPerMinute = e.BreathsPerMinute;
            return this;
        }

        public Eugene Build()
        {
            // 集中校验：可根据业务调整阈值
            if (string.IsNullOrWhiteSpace(_name))
                throw new ArgumentException("Name cannot be empty.");

            if (_age is < 0 or > 150)
                throw new ArgumentOutOfRangeException(nameof(_age), "Age out of range.");

            if (_breathsPerMinute is < 5 or > 80)
                throw new ArgumentOutOfRangeException(nameof(_breathsPerMinute), "BPM out of range.");

            return new Eugene(_name, _age, _breathsPerMinute);
        }
    }
}
