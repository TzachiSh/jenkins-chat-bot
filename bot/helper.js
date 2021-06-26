const fetch = require('node-fetch')
const fetchWebHook = (url, details) => {
  fetch(url, {
    method: 'POST',
    body: JSON.stringify(details)
}).then(results => {
  results.text().then( res => {
    console.log('res',res);
  })
});
}
const sleep = (ms) => new Promise(resolve => setTimeout(resolve, ms));

const objToString = (obj) => {
  let str = '';
  for (const [p, val] of Object.entries(obj)) {
      str += ` ${p}: ${val} \n`;
  }
  return str;
}

module.exports = {
  fetchWebHook,
  sleep,
  objToString
}