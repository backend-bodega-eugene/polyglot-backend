using Utils.Builder;

public sealed class EugeneBuilder
{
    public string Name { get; }
    public int Age { get; }
    public int BreathsPerMinute { get; }

    public EugeneBuilder(string name = "laohu", int age = 18, int bpm = 20)
        => (Name, Age, BreathsPerMinute) = (name, age, bpm);

    public EugeneBuilder WithName(string name) => new(name, Age, BreathsPerMinute);
    public EugeneBuilder WithAge(int age) => new(Name, age, BreathsPerMinute);
    public EugeneBuilder WithBreathsPerMinute(int bpm) => new(Name, Age, bpm);

    public Eugene Build()
    {
        if (string.IsNullOrWhiteSpace(Name)) throw new ArgumentException("Name cannot be empty.");
        if (Age is < 0 or > 150) throw new ArgumentOutOfRangeException(nameof(Age));
        if (BreathsPerMinute is < 5 or > 80) throw new ArgumentOutOfRangeException(nameof(BreathsPerMinute));
        // 使用 Eugene 的 Builder 构建实例，避免直接调用不可访问的构造函数
        return Eugene.New()
            .WithName(Name)
            .WithAge(Age)
            .WithBreathsPerMinute(BreathsPerMinute)
            .Build();
    }
}
