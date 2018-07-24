import React from 'react';
import ReactDOM from 'react-dom';
import "typeface-roboto"
import './index.css';
import App from './App';
import registerServiceWorker from './registerServiceWorker';

import './images'

import 'react-circular-progressbar/dist/styles.css';

const rootEl = document.getElementById('root');

ReactDOM.render(<App />, rootEl);
registerServiceWorker();

if (module.hot) {
  module.hot.accept('./App', () => {
    const NextApp = require('./App').default
    ReactDOM.render(
      <NextApp />,
      rootEl
    )
  })
}
