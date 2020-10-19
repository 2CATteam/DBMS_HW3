CREATE PROCEDURE option1(@fid AS INT, @name AS VARCHAR(64), @dept_id AS INT) --Bam, we got our function declaration
AS
BEGIN
	DECLARE @avg_salary REAL; --Variable for storing the average salary
	SELECT @avg_salary = AVG(salary) FROM Faculty WHERE deptid = @dept_id; --Store average salary
	IF @avg_salary > 50000 --Insert values based on avg_salary
	BEGIN
		INSERT INTO Faculty VALUES (@fid, @name, @dept_id, @avg_salary * 0.9); --If the salary is too high, being it down a bit
	END
	ELSE IF @avg_salary < 30000
	BEGIN
		INSERT INTO Faculty VALUES (@fid, @name, @dept_id, @avg_salary); --If the salary is too low, keep it
	END
	ELSE
	BEGIN
		INSERT INTO Faculty VALUES (@fid, @name, @dept_id, @avg_salary * 0.8); --If the salary is just right, bring it down a lot
	END
END;

