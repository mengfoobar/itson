<?php
	 
//load and connect to MySQL database stuff
require("config.inc.php");

if (!empty($_POST)) {

	$username=$_POST['username'];
	$password=$_POST['password']
	
    //gets user's info based off of a username.
    $query = "
            SELECT
                user_id,
                username,
                password
            FROM users
	            WHERE
                username = '$username'
	        ";
     


    try {
        $stmt   = $db->prepare($query); //HL: prepares the query. what is $db, $stmt
        $stmt->execute();  //executes the query
    }
    catch (PDOException $ex) {
        // For testing, you could use a die and message.
        //die("Failed to run query: " . $ex->getMessage());
        
        //or just use this use this one to product JSON data:
        $response["success"] = 0;
        $response["message"] = "Error Accessing Database";
        die(json_encode($response));  //what does the die do here? turns off the script?
         
    }
     
    //This will be the variable to determine whether or not the user's information is correct.
    //we initialize it as false.
    $validated_info = false;
     
    //fetching all the rows from the query
    $row = $stmt->fetch();  //HL: we don't have to declare variables in PHP?
	
    if ($row) {
        //if we encrypted the password, we would unencrypt it here, but in our case we just
        //compare the two passwords
		$response["posts"]   = array();
		
		if($password!=$row["password"])
		{
			$response["success"] = 0;
			$response["message"] = "Invalid Password!";
			die(json_encode($response));
		}
		else
		{
			$post = array();
			$post["user_id"]    = $row["user_id"]; //check if this becomes an integer
			$response["success"] = 1;
			$response["message"] = "Login successful!";
			
			array_push($response["posts"], $post);
			
			echo json_encode($response);
		}	
		

    }
	else
	{
		$response["success"] = 0;
		$response["message"] = "Invalid Credentials!";
		die(json_encode($response));
	}

} else {
//HL: why is this else placed here? does it mean the script is done?
?>
        <h1>Login</h1>
        <form action="login.php" method="post">
            Username:<br />
            <input type="text" name="username" placeholder="username" />
            <br /><br />
            Password:<br />
            <input type="password" name="password" placeholder="password" value="" />
            <br /><br />
            <input type="submit" value="Login" />
        </form>
        <a href="register.php">Register</a>
    <?php
} 
?> 