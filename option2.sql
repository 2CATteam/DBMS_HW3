CREATE PROCEDURE option2(@fid AS INT, @name AS VARCHAR(64), @dept_id AS INT, @ignore_dept AS INT) --Function declaration
AS
BEGIN
	DECLARE @avg_salary REAL; --Store salary in this variable
	SELECT @avg_salary = AVG(salary) FROM Faculty WHERE deptid != @ignore_dept; --Get average salary
	INSERT INTO Faculty VALUES (@fid, @name, @dept_id, @avg_salary); --Insert values
END;