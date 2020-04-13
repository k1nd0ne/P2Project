<!doctype html>
<html lang="en">
<head>
  <!-- Required meta tags -->
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">

  <!-- Bootstrap CSS -->
  <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/css/bootstrap.min.css" integrity="sha384-Vkoo8x4CGsO3+Hhxv8T/Q5PaXtkKtu6ug5TOeNV6gBiFeWPGFN9MuhOf23Q9Ifjh" crossorigin="anonymous">

  <title>P2P - Client</title>
  <style>

    ul, li{
      display:inline;
    }
  </style>
</head>



<body>


  <!-- Optional JavaScript -->
  <!-- jQuery first, then Popper.js, then Bootstrap JS -->
  <script src="https://code.jquery.com/jquery-3.4.1.slim.min.js" integrity="sha384-J6qa4849blE2+poT4WnyKhv5vZF5SrPo0iEjwBvKU7imGFAV0wwj1yYfoRSJoZ+n" crossorigin="anonymous"></script>
  <script src="https://cdn.jsdelivr.net/npm/popper.js@1.16.0/dist/umd/popper.min.js" integrity="sha384-Q6E9RHvbIyZFJoft+2mJbHaEWldlvI9IOYy5n3zV9zzTtmI3UksdQRVvoxMfooAo" crossorigin="anonymous"></script>
  <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.4.1/js/bootstrap.min.js" integrity="sha384-wfSDF2E50Y2D1uUdj0O3uMBJnjuUD4Ih7YwaYd1iqfktj0Uod8GCExl3Og8ifwB6" crossorigin="anonymous"></script>

  <div class="container">
    <div class="jumbotron" style="text-align: center">
      <h1>The P2P Bay</h1>
      <p>An open P2P client to download and share file</p>
      <form class="card card-sm">
                               <div class="card-body row no-gutters align-items-center">
                                   <div class="col-auto">
                                       <i class="fas fa-search h4 text-body"></i>
                                   </div>
                                   <!--end of col-->
                                   <div class="col">
                                       <input class="form-control form-control-lg form-control-borderless" type="search" placeholder="Search for a file...">
                                   </div>
                                   <!--end of col-->
                                   <div class="col-auto">
                                       <button class="btn btn-lg btn-success" type="submit">Search</button>
                                   </div>
                                   <!--end of col-->
                               </div>
                           </form>
    </div>

  <h4>File you are sharing</h4>
<div class="row">
  <table class="table table-dark">
    <thead>
      <tr>
        <th scope="col">#</th>
        <th scope="col">Filename</th>
        <th scope="col">Connected Client</th>
      </tr>
    </thead>
    <tbody>
      <?php
        exec("ls -p ../../src/loot | grep -v / ",$array);
        for ($i = 0; $i < count($array); $i++) {
          echo "
          <tr>
            <th scope='row'>"; echo ($i+1); echo "</th>"; echo "
            <td>$array[$i]</td>
            <td>n/a</td>
          </tr>
          ";
        }
      ?>
    </tbody>
  </table>
</div>




    </div>

  </<body>

</html>
