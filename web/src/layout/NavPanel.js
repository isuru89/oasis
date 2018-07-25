import React, { Component } from 'react'
import styled from 'styled-components';
import NavButton from './NavButton';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'

const Wrapper = styled.div`
  background-color: #20465E;
`

const BUTTONS = [
  { id: 'profile', label: 'Profile', image: 'address-card' },
  { id: 'contests', label: 'Contests', image: 'gamepad' },
  { id: 'progress', label: 'Progress', image: 'chart-line' },
  { id: 'badges', label: 'Badges', image: 'medal' },
  { id: 'store', label: 'Store', image: 'store-alt' },
]

export default class NavPanel extends Component {

  state = {
    selected: 'profile'
  }

  render() {
    return (
      <Wrapper>
        {
          BUTTONS.map(b => <NavButton key={b.id} 
            id={b.id}
            label={b.id} 
            selected={b.id === this.state.selected} 
            onClick={this._whenButtonClicked}
            image={<FontAwesomeIcon icon={b.image} />} />)
        }
      </Wrapper>
    )
  }

  _whenButtonClicked = id => {
    this.setState({ selected: id })
  }
}
