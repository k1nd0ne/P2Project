<?php
$url=$_SERVER['REQUEST_URI'];
header("Refresh: 60; URL=$url");
?>
<!doctype html>
<html lang="en">
<head>
  <!-- Required meta tags -->
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">

  <!-- Bootstrap CSS -->
  <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/css/bootstrap.min.css" integrity="sha384-Vkoo8x4CGsO3+Hhxv8T/Q5PaXtkKtu6ug5TOeNV6gBiFeWPGFN9MuhOf23Q9Ifjh" crossorigin="anonymous">

  <title>P2P - MasterRepository</title>
  <style>

  ul, li{
    display:inline;
  }
</style>
<script src="https://canvasjs.com/assets/script/canvasjs.min.js"></script>
<script>
window.onload = function () {

var chart = new CanvasJS.Chart("chartContainer", {
	animationEnabled: true,
	theme: "light2",
	title:{
		text: "Client number in time"
	},
	axisY:{
		includeZero: false
	},
	data: [{
		type: "line",
      	indexLabelFontSize: 16,
		dataPoints: [
      <?php
      $handle = fopen("graph", "r");
      if ($handle) {
        while (($line = fgets($handle)) !== false) {
          echo $line;
        }

        fclose($handle);
      }
      ?>

		]
	}]
});
chart.render();

}
</script>

</head>



<body>


  <!-- Optional JavaScript -->
  <!-- jQuery first, then Popper.js, then Bootstrap JS -->
  <script src="https://code.jquery.com/jquery-3.4.1.slim.min.js" integrity="sha384-J6qa4849blE2+poT4WnyKhv5vZF5SrPo0iEjwBvKU7imGFAV0wwj1yYfoRSJoZ+n" crossorigin="anonymous"></script>
  <script src="https://cdn.jsdelivr.net/npm/popper.js@1.16.0/dist/umd/popper.min.js" integrity="sha384-Q6E9RHvbIyZFJoft+2mJbHaEWldlvI9IOYy5n3zV9zzTtmI3UksdQRVvoxMfooAo" crossorigin="anonymous"></script>
  <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/js/bootstrap.min.js" integrity="sha384-wfSDF2E50Y2D1uUdj0O3uMBJnjuUD4Ih7YwaYd1iqfktj0Uod8GCExl3Og8ifwB6" crossorigin="anonymous"></script>
  <div class="container">
    <div class="jumbotron">
      <h1>P2P Administration Dashboard</h1>
      <p>This is the master Repository Console, you can view your database content as well as P2P Client</p>
    </div>

        <div class="row">
          <div class="card text-white bg-success mb-3" style="margin-bottom: 3rem;width: 10rem;max-height: 3rem;margin-left:1rem;">
            <div class="card-header" id="clientCount">
              <?php
              $myfile = fopen("clientCount", "r") or die("Unable to open file!");
              echo fread($myfile,filesize("clientCount"));
              fclose($myfile);
              ?>
            </div>

          </div>

          <div class="card text-white bg-primary mb-3" style="width: 10rem; max-height: 3rem;">
            <div class="card-header" id="fileCount">
              <?php
              $myfile = fopen("fileCount", "r") or die("Unable to open file!");
              echo fread($myfile,filesize("fileCount"));
              fclose($myfile);
              ?>
            </div>
          </div>
        </div>
    <div class="row">
      <div class="col-sm-3">
        <h3>Database Content : </h3>
        <table class="table table-dark">
          <thead>
            <tr>
              <th scope="col">#</th>
            </tr>
          </thead>
          <tbody>
            <?php
            $handle = fopen("db", "r");
            if ($handle) {
              while (($line = fgets($handle)) !== false) {
                echo "
                </tr>
                <th> $line </th>
                </tr>
                ";

              }

              fclose($handle);
            }
            ?>

          </tbody>
        </table>
      </div>
      <div class="col-sm-9">
        <h3>Server Logs : </h3>
        <textarea class="form-control" id="logResult" rows="20">
          <?php
          $handle = fopen("serverLog", "r");
          if ($handle) {
            while (($line = fgets($handle)) !== false) {
              echo $line;
            }

            fclose($handle);
          }
          ?>
        </textarea>
      </div>

    </div>
    <div id="chartContainer" style="height: 100%; width: 100%;"></div>
  </div>

</body>

</html>
