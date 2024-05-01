/*  Where contents of data.csv is:
username,password
admin,123
test_user,1234
*/
import http from 'k6/http';
import { check, sleep } from 'k6';
import { SharedArray } from 'k6/data';
import papaparse from 'https://jslib.k6.io/papaparse/5.1.1/index.js';

// not using SharedArray here will mean that the code in the function call (that is what loads and
// parses the csv) will be executed per each VU which also means that there will be a complete copy
// per each VU
const csvData = new SharedArray('another data name', function () {
  // Load CSV file and parse it using Papa Parse
  return papaparse.parse(open('./data.csv'), { header: true }).data;
});

export const options = {
  vus: 20,
  duration: '10s',
};

export default function () {
  // Now you can use the CSV data in your test logic below.
  // Below are some examples of how you can access the CSV data.


  // Pick a random username/password pair
  const randomUser = csvData[Math.floor(Math.random() * csvData.length)];
  //console.log('Random user: ', JSON.stringify(randomUser));
  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  const payload = {
    name: randomUser.name,
    message: randomUser.message,
  };
  //console.log('Random user: ', JSON.stringify(params));

  const res = http.post('http://localhost:8282/api/says', JSON.stringify(payload), params);
  check(res, {
    'login succeeded': (r) => r.status === 200,
  });

  sleep(1);
}
