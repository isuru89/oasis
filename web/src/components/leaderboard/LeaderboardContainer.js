import React, { Component } from 'react'
import Leaderboard from "./Leaderboard";

const rks = [
  { userId: 100, name: 'Cersi Lannister', points: (Math.random() * 100000).toFixed(0) },
  { userId: 101, name: 'Jon Snow', points: (Math.random() * 100000).toFixed(0) },
  { userId: 102, name: 'Arya Stark', points: (Math.random() * 100000).toFixed(0) },
  { userId: 103, name: 'Denarys Targerian', points: (Math.random() * 100000).toFixed(0) },
  { userId: 104, name: 'Sansa Stark', points: (Math.random() * 100000).toFixed(0) },
  { userId: 105, name: 'Cersi Lannister', points: (Math.random() * 100000).toFixed(0) },
  { userId: 106, name: 'Jon Snow', points: (Math.random() * 100000).toFixed(0) },
  { userId: 107, name: 'Arya Stark', points: (Math.random() * 100000).toFixed(0) },
  { userId: 108, name: 'Denarys Targerian', points: (Math.random() * 100000).toFixed(0) },
  { userId: 109, name: 'Sansa Stark', points: (Math.random() * 100000).toFixed(0) },
]

export default class LeaderboardContainer extends Component {

  state = {
    activePeriod: 'daily'
  }

  render() {
    rks.sort((a,b) => b.points - a.points);
    let r = 1;
    rks.forEach(rc => { if (!rc.rank) rc.rank = r++ });

    return (
      <Leaderboard records = {
        rks
      }
      headerRecord = {
        rks[(Math.random() * rks.length).toFixed(0)]
      }
      activePeriod={this.state.activePeriod}
      whenTimeRangeClicked={this._whenTimeChanged}
      />
    )
  }

  _whenTimeChanged = tid => {
    this.setState({ activePeriod: tid });
  }
}
